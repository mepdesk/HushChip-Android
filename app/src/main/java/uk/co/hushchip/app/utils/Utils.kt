package uk.co.hushchip.app.utils

import androidx.compose.runtime.MutableState
import org.bitcoinj.crypto.MnemonicCode
import org.satochip.client.seedkeeper.SeedkeeperSecretType
import uk.co.hushchip.app.R

const val TAG = "Utils"

fun bytesToHex(bytes: ByteArray) : String {
    return bytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
}

fun getSeedQr(mnemonic: String): String {
    // todo add support for other wordlist languages
    // based on https://github.com/SeedSigner/seedsigner/blob/dev/docs/seed_qr/README.md#standard-seedqr-specification
    val indices = mnemonic.split(" ").map { word ->
        MnemonicCode.INSTANCE.wordList.indexOf(word).also {
            if (it == -1){
                throw IllegalArgumentException("Word not found in BIP-39 wordlist: $word")
            }
        }
    }
    val mnemonicDecimalString = indices.joinToString(separator = "") { index ->
        index.toString(10).padStart(4, '0')
    }
    return mnemonicDecimalString
}

fun isClickable(
    secret: MutableState<String>,
    curValueLabel: MutableState<String>
): Boolean {
    return secret.value.isNotEmpty()
            && curValueLabel.value.isNotEmpty()
}

fun getDrawableIdFromType(type: SeedkeeperSecretType): Int {
    return when (type) {
        SeedkeeperSecretType.PASSWORD -> {
            R.drawable.password_icon
        }
        SeedkeeperSecretType.MASTERSEED, SeedkeeperSecretType.BIP39_MNEMONIC -> {
            R.drawable.mnemonic
        }
        SeedkeeperSecretType.ELECTRUM_MNEMONIC -> {
            R.drawable.atom_light
        }
        SeedkeeperSecretType.DATA -> {
            R.drawable.free_data
        }
        SeedkeeperSecretType.WALLET_DESCRIPTOR -> {
            R.drawable.wallet
        }
        SeedkeeperSecretType.PUBKEY -> {
            R.drawable.key
        }
        else -> {
            R.drawable.key
        }
    }
}

val instructionsMap: Map<Byte, String> = mapOf(
    0x2A.toByte() to "Setup",
    0x32.toByte() to "Import key",
    0x33.toByte() to "Reset key",
    0x35.toByte() to "Get public from private",
    0x40.toByte() to "Create pin",
    0x42.toByte() to "Verify pin",
    0x44.toByte() to "Change pin",
    0x46.toByte() to "Unblock pin",
    0x60.toByte() to "Logout all",
    0x48.toByte() to "List pins",
    0x3C.toByte() to "Get status",
    0x3D.toByte() to "Card label",
    0x6C.toByte() to "Bip32 import seed",
    0x77.toByte() to "Bip32 reset seed",
    0x73.toByte() to "Bip32 get authentikey",
    0x75.toByte() to "Bip32 set authentikey pubkey",
    0x6D.toByte() to "Bip32 get extended key",
    0x74.toByte() to "Bip32 set extended pubkey",
    0x6E.toByte() to "Sign message",
    0x72.toByte() to "Sign short message",
    0x6F.toByte() to "Sign transaction",
    0x71.toByte() to "Parse transaction",
    0x76.toByte() to "Crypt transaction 2fa",
    0x79.toByte() to "Set 2fa key",
    0x78.toByte() to "Reset 2fa key",
    0x7A.toByte() to "Sign transaction hash",
    0x81.toByte() to "Init secure channel",
    0x82.toByte() to "Process secure channel",
    0xAC.toByte() to "Import encrypted secret",
    0xAA.toByte() to "Import trusted pubkey",
    0xAB.toByte() to "Export trusted pubkey",
    0xAD.toByte() to "Export authentikey",
    0x92.toByte() to "Import pki certificate",
    0x93.toByte() to "Export pki certificate",
    0x94.toByte() to "Sign pki csr",
    0x98.toByte() to "Export pki pubkey",
    0x99.toByte() to "Lock pki",
    0x9A.toByte() to "Challenge response pki",
    0xFF.toByte() to "Reset to factory",
    0xA7.toByte() to "Get card status",
    0xA0.toByte() to "Generate master seed",
    0xA3.toByte() to "Generate random secret",
    0xAE.toByte() to "Generate 2fa secret",
    0xA1.toByte() to "Import secret",
    0xA2.toByte() to "Export secret",
    0xA8.toByte() to "Export secret to backup",
    0xA5.toByte() to "Reset secret",
    0xA6.toByte() to "List secret headers",
    0xA9.toByte() to "Print card logs",
    0xAF.toByte() to "Derive master password"
)