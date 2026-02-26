package uk.co.hushchip.app.utils

fun String.countWords(): Int {
    return this.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
}

fun String.toMnemonicList(): List<String?> {
    return this.split("\\s+".toRegex())
}