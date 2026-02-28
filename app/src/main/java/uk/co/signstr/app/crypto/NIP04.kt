package uk.co.signstr.app.crypto

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * NIP-04 legacy encryption.
 * ECDH shared secret (raw, no HKDF), AES-256-CBC.
 * Format: base64(ciphertext)?iv=base64(iv)
 */
object NIP04 {

    fun encrypt(privkey: ByteArray, pubkeyHex: String, plaintext: String): String {
        val pubkeyBytes = NIP44.hexToBytes(pubkeyHex)
        val sharedSecret = Secp256k1.ecdh(privkey, pubkeyBytes)

        val iv = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(sharedSecret, "AES"), IvParameterSpec(iv))
        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        val ctBase64 = android.util.Base64.encodeToString(ciphertext, android.util.Base64.NO_WRAP)
        val ivBase64 = android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP)
        return "$ctBase64?iv=$ivBase64"
    }

    fun decrypt(privkey: ByteArray, pubkeyHex: String, payload: String): String {
        val pubkeyBytes = NIP44.hexToBytes(pubkeyHex)
        val sharedSecret = Secp256k1.ecdh(privkey, pubkeyBytes)

        val parts = payload.split("?iv=")
        if (parts.size != 2) throw IllegalArgumentException("Invalid NIP-04 payload format")

        val ciphertext = android.util.Base64.decode(parts[0], android.util.Base64.DEFAULT)
        val iv = android.util.Base64.decode(parts[1], android.util.Base64.DEFAULT)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(sharedSecret, "AES"), IvParameterSpec(iv))
        return String(cipher.doFinal(ciphertext), Charsets.UTF_8)
    }
}
