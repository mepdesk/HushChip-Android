package uk.co.signstr.app.crypto

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Stores nsec private keys in Android Keystore (hardware-backed on supported devices).
 * Each identity gets its own AES key in the Keystore. The nsec is encrypted with AES-GCM
 * and stored in SharedPreferences.
 */
object SignstrKeyStore {
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val PREFS_NAME = "signstr_keys"
    private const val KEY_PREFIX = "signstr_key_"
    private const val IV_SUFFIX = "_iv"
    private const val DATA_SUFFIX = "_data"

    private fun getOrCreateKey(identityId: String): SecretKey {
        val alias = KEY_PREFIX + identityId
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (keyStore.containsAlias(alias)) {
            return keyStore.getKey(alias, null) as SecretKey
        }

        val keyGen = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        keyGen.init(
            KeyGenParameterSpec.Builder(alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false)
                .build()
        )
        return keyGen.generateKey()
    }

    fun storePrivateKey(context: Context, identityId: String, privkey: ByteArray) {
        val key = getOrCreateKey(identityId)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encrypted = cipher.doFinal(privkey)

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(identityId + IV_SUFFIX, android.util.Base64.encodeToString(iv, android.util.Base64.NO_WRAP))
            .putString(identityId + DATA_SUFFIX, android.util.Base64.encodeToString(encrypted, android.util.Base64.NO_WRAP))
            .apply()
    }

    fun loadPrivateKey(context: Context, identityId: String): ByteArray? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val ivStr = prefs.getString(identityId + IV_SUFFIX, null) ?: return null
        val dataStr = prefs.getString(identityId + DATA_SUFFIX, null) ?: return null

        val iv = android.util.Base64.decode(ivStr, android.util.Base64.NO_WRAP)
        val encrypted = android.util.Base64.decode(dataStr, android.util.Base64.NO_WRAP)

        val alias = KEY_PREFIX + identityId
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        if (!keyStore.containsAlias(alias)) return null

        val key = keyStore.getKey(alias, null) as SecretKey
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
        return cipher.doFinal(encrypted)
    }

    fun deletePrivateKey(context: Context, identityId: String) {
        val alias = KEY_PREFIX + identityId
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
        }
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(identityId + IV_SUFFIX)
            .remove(identityId + DATA_SUFFIX)
            .apply()
    }
}
