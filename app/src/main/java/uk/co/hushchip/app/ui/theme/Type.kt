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

package uk.co.hushchip.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import uk.co.hushchip.app.R

val outfitFamily = FontFamily(
    Font(R.font.outfit_extralight, FontWeight.ExtraLight),
    Font(R.font.outfit_light, FontWeight.Light),
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_medium, FontWeight.Medium),
)

val Typography = Typography(
    displayLarge = TextStyle(fontFamily = outfitFamily, fontWeight = FontWeight.Light, fontSize = 16.sp, letterSpacing = 0.5.sp),
    titleLarge = TextStyle(fontFamily = outfitFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    titleMedium = TextStyle(fontFamily = outfitFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp, letterSpacing = 5.sp),
    bodyLarge = TextStyle(fontFamily = outfitFamily, fontWeight = FontWeight.Light, fontSize = 12.sp),
    bodyMedium = TextStyle(fontFamily = outfitFamily, fontWeight = FontWeight.Light, fontSize = 11.sp),
    labelLarge = TextStyle(fontFamily = outfitFamily, fontWeight = FontWeight.Normal, fontSize = 11.sp, letterSpacing = 4.sp),
    labelMedium = TextStyle(fontFamily = outfitFamily, fontWeight = FontWeight.Normal, fontSize = 9.sp, letterSpacing = 3.sp),
    labelSmall = TextStyle(fontFamily = outfitFamily, fontWeight = FontWeight.Normal, fontSize = 9.sp, letterSpacing = 1.sp),
)
