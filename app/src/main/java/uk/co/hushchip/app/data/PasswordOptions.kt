// Copyright (c) 2026 Gridmark Technologies Ltd (HushChip)
// https://github.com/hushchip/HushChip-Android
//
// Based on Seedkeeper-Android by Toporin / Satochip S.R.L.
// https://github.com/Toporin/Seedkeeper-Android
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

package uk.co.hushchip.app.data

data class PasswordOptions(
    var isLowercaseSelected: Boolean = true,
    var isUppercaseSelected: Boolean = true,
    var isNumbersSelected: Boolean = true,
    var isSymbolsSelected: Boolean = true,
    var isMemorableSelected: Boolean = false,
    var passwordLength: Int = 12
)