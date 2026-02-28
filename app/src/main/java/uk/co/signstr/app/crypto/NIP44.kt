package uk.co.signstr.app.crypto

import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * NIP-44 versioned, padded, authenticated encryption.
 *
 * CRITICAL: conversationKey uses HKDF-extract ONLY (not extract+expand).
 * Uses pure Kotlin ChaCha20 (NOT ChaCha20-Poly1305 AEAD) for API 26+ compat.
 * Authentication is via separate HMAC-SHA256.
 */
object NIP44 {

    /**
     * Derive the conversation key between two parties.
     * HKDF-extract ONLY: HMAC-SHA256(key="nip44-v2", data=shared_x)
     */
    fun conversationKey(privkey: ByteArray, pubkeyHex: String): ByteArray {
        val pubkeyBytes = hexToBytes(pubkeyHex)
        val sharedX = Secp256k1.ecdh(privkey, pubkeyBytes)
        val salt = "nip44-v2".toByteArray(Charsets.UTF_8)
        return hmacSha256(salt, sharedX)
    }

    /**
     * Derive message keys using HKDF-expand.
     * PRK = conversation_key, info = nonce, L = 76
     */
    private fun deriveMessageKeys(conversationKey: ByteArray, nonce: ByteArray): Triple<ByteArray, ByteArray, ByteArray> {
        val expanded = hkdfExpand(conversationKey, nonce, 76)
        val chachaKey = expanded.copyOfRange(0, 32)
        val chachaNonce = expanded.copyOfRange(32, 44)
        val hmacKey = expanded.copyOfRange(44, 76)
        return Triple(chachaKey, chachaNonce, hmacKey)
    }

    fun encrypt(conversationKey: ByteArray, plaintext: String): String {
        val nonce = ByteArray(32).also { SecureRandom().nextBytes(it) }
        val (chachaKey, chachaNonce, hmacKey) = deriveMessageKeys(conversationKey, nonce)

        val padded = pad(plaintext)
        val ciphertext = chacha20(chachaKey, chachaNonce, padded)

        val payload = nonce + ciphertext
        val mac = hmacSha256(hmacKey, payload)

        val result = byteArrayOf(0x02) + payload + mac
        return android.util.Base64.encodeToString(result, android.util.Base64.NO_WRAP)
    }

    fun decrypt(conversationKey: ByteArray, payload: String): String {
        val raw = android.util.Base64.decode(payload, android.util.Base64.DEFAULT)
        if (raw.isEmpty() || raw[0] != 0x02.toByte()) {
            throw IllegalArgumentException("Unknown NIP-44 version: ${if (raw.isNotEmpty()) raw[0] else "empty"}")
        }

        val nonce = raw.copyOfRange(1, 33)
        val ciphertext = raw.copyOfRange(33, raw.size - 32)
        val mac = raw.copyOfRange(raw.size - 32, raw.size)

        val (chachaKey, chachaNonce, hmacKey) = deriveMessageKeys(conversationKey, nonce)

        // Verify HMAC first (before decryption)
        val expectedMac = hmacSha256(hmacKey, nonce + ciphertext)
        if (!constantTimeEquals(mac, expectedMac)) {
            throw SecurityException("NIP-44 HMAC verification failed")
        }

        val padded = chacha20(chachaKey, chachaNonce, ciphertext)
        return unpad(padded)
    }

    private fun pad(plaintext: String): ByteArray {
        val utf8 = plaintext.toByteArray(Charsets.UTF_8)
        val len = utf8.size
        if (len < 1 || len > 65535) throw IllegalArgumentException("Plaintext too long")

        val paddedLen = calcPaddedLen(len)
        val result = ByteArray(2 + paddedLen)
        result[0] = (len shr 8).toByte()
        result[1] = (len and 0xFF).toByte()
        System.arraycopy(utf8, 0, result, 2, len)
        return result
    }

    private fun unpad(padded: ByteArray): String {
        val len = ((padded[0].toInt() and 0xFF) shl 8) or (padded[1].toInt() and 0xFF)
        if (len < 1 || len > padded.size - 2) throw IllegalArgumentException("Invalid padding")
        return String(padded, 2, len, Charsets.UTF_8)
    }

    private fun calcPaddedLen(unpaddedLen: Int): Int {
        if (unpaddedLen <= 32) return 32
        val nextPow2 = Integer.highestOneBit(unpaddedLen - 1) shl 1
        val chunk = maxOf(32, nextPow2 / 8)
        return chunk * ((unpaddedLen - 1) / chunk + 1)
    }

    // --- Pure Kotlin ChaCha20 (NOT AEAD — NIP-44 uses raw ChaCha20 + separate HMAC) ---

    private fun chacha20(key: ByteArray, nonce: ByteArray, data: ByteArray): ByteArray {
        val output = ByteArray(data.size)
        var counter = 0
        var offset = 0

        while (offset < data.size) {
            val block = chacha20Block(key, counter, nonce)
            val remaining = minOf(64, data.size - offset)
            for (i in 0 until remaining) {
                output[offset + i] = (data[offset + i].toInt() xor block[i].toInt()).toByte()
            }
            offset += remaining
            counter++
        }

        return output
    }

    private fun chacha20Block(key: ByteArray, counter: Int, nonce: ByteArray): ByteArray {
        val state = IntArray(16)

        // Constants: "expand 32-byte k"
        state[0] = 0x61707865
        state[1] = 0x3320646e
        state[2] = 0x79622d32
        state[3] = 0x6b206574

        // Key (8 words, little-endian)
        for (i in 0..7) {
            state[4 + i] = littleEndianToInt(key, i * 4)
        }

        // Counter
        state[12] = counter

        // Nonce (3 words, little-endian)
        for (i in 0..2) {
            state[13 + i] = littleEndianToInt(nonce, i * 4)
        }

        // Working copy
        val working = state.copyOf()

        // 20 rounds (10 iterations of column + diagonal)
        repeat(10) {
            // Column rounds
            quarterRound(working, 0, 4, 8, 12)
            quarterRound(working, 1, 5, 9, 13)
            quarterRound(working, 2, 6, 10, 14)
            quarterRound(working, 3, 7, 11, 15)
            // Diagonal rounds
            quarterRound(working, 0, 5, 10, 15)
            quarterRound(working, 1, 6, 11, 12)
            quarterRound(working, 2, 7, 8, 13)
            quarterRound(working, 3, 4, 9, 14)
        }

        // Add original state
        for (i in 0..15) {
            working[i] += state[i]
        }

        // Serialize to bytes (little-endian)
        val result = ByteArray(64)
        for (i in 0..15) {
            intToLittleEndian(working[i], result, i * 4)
        }
        return result
    }

    private fun quarterRound(s: IntArray, a: Int, b: Int, c: Int, d: Int) {
        s[a] += s[b]; s[d] = (s[d] xor s[a]).rotateLeft(16)
        s[c] += s[d]; s[b] = (s[b] xor s[c]).rotateLeft(12)
        s[a] += s[b]; s[d] = (s[d] xor s[a]).rotateLeft(8)
        s[c] += s[d]; s[b] = (s[b] xor s[c]).rotateLeft(7)
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun Int.rotateLeft(bits: Int): Int =
        (this shl bits) or (this ushr (32 - bits))

    private fun littleEndianToInt(bs: ByteArray, off: Int): Int =
        (bs[off].toInt() and 0xFF) or
        ((bs[off + 1].toInt() and 0xFF) shl 8) or
        ((bs[off + 2].toInt() and 0xFF) shl 16) or
        ((bs[off + 3].toInt() and 0xFF) shl 24)

    private fun intToLittleEndian(n: Int, bs: ByteArray, off: Int) {
        bs[off] = n.toByte()
        bs[off + 1] = (n ushr 8).toByte()
        bs[off + 2] = (n ushr 16).toByte()
        bs[off + 3] = (n ushr 24).toByte()
    }

    // --- End ChaCha20 ---

    /**
     * HKDF-expand (RFC 5869 Section 2.3)
     */
    private fun hkdfExpand(prk: ByteArray, info: ByteArray, length: Int): ByteArray {
        val hashLen = 32
        val n = (length + hashLen - 1) / hashLen
        var t = byteArrayOf()
        val okm = ByteArray(length)
        var offset = 0
        for (i in 1..n) {
            t = hmacSha256(prk, t + info + byteArrayOf(i.toByte()))
            val copyLen = minOf(hashLen, length - offset)
            System.arraycopy(t, 0, okm, offset, copyLen)
            offset += copyLen
        }
        return okm
    }

    fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }

    private fun constantTimeEquals(a: ByteArray, b: ByteArray): Boolean {
        if (a.size != b.size) return false
        var diff = 0
        for (i in a.indices) {
            diff = diff or (a[i].toInt() xor b[i].toInt())
        }
        return diff == 0
    }

    fun hexToBytes(hex: String): ByteArray {
        val cleanHex = hex.lowercase().removePrefix("0x")
        return ByteArray(cleanHex.length / 2) { i ->
            cleanHex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
    }

    fun bytesToHex(bytes: ByteArray): String =
        bytes.joinToString("") { "%02x".format(it) }
}
