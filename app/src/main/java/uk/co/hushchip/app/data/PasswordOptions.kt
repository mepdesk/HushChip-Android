package uk.co.hushchip.app.data

data class PasswordOptions(
    var isLowercaseSelected: Boolean = true,
    var isUppercaseSelected: Boolean = true,
    var isNumbersSelected: Boolean = true,
    var isSymbolsSelected: Boolean = true,
    var isMemorableSelected: Boolean = false,
    var passwordLength: Int = 12
)