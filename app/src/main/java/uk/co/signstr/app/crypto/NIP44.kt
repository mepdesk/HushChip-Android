package uk.co.signstr.app.crypto

import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * NIP-44 versioned, padded, authenticated encryption.
 *
 * CRITICAL: conversationKey uses HKDF-extract ONLY (not extract+expand).
 * See SIGNSTR_NIP46_REFERENCE.md for the bug that blocked everything.
 */
object NIP44 {

    /**
     * Derive the conversation key between two parties.
     * HKDF-extract ONLY: HMAC-SHA256(key="nip44-v2", data=shared_x)
     */
    fun conversationKey(privkey: ByteArray, pubkeyHex: String): ByteArray {
        val pubkeyBytes = hexToBytes(pubkeyHex)
        val sharedX = Secp256k1.ecdh(privkey, pubkeyBytes)
        // HKDF-extract ONLY — NOT extract+expand
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
        val ciphertext = chacha20Poly1305Encrypt(chachaKey, chachaNonce, padded)

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

        // Verify HMAC
        val expectedMac = hmacSha256(hmacKey, nonce + ciphertext)
        if (!mac.contentEquals(expectedMac)) {
            throw SecurityException("NIP-44 HMAC verification failed")
        }

        val padded = chacha20Poly1305Decrypt(chachaKey, chachaNonce, ciphertext)
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

    private fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key, "HmacSHA256"))
        return mac.doFinal(data)
    }

    private fun chacha20Poly1305Encrypt(key: ByteArray, nonce: ByteArray, plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("ChaCha20-Poly1305")
        val keySpec = SecretKeySpec(key, "ChaCha20")
        val ivSpec = IvParameterSpec(nonce)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(plaintext)
    }

    private fun chacha20Poly1305Decrypt(key: ByteArray, nonce: ByteArray, ciphertext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("ChaCha20-Poly1305")
        val keySpec = SecretKeySpec(key, "ChaCha20")
        val ivSpec = IvParameterSpec(nonce)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        return cipher.doFinal(ciphertext)
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
