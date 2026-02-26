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

package uk.co.hushchip.app.ui.components.backup

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import uk.co.hushchip.app.ui.theme.HushColors
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun BackupSingleText(
    text: Int,
    fontWeight: FontWeight = FontWeight.ExtraLight
) {
    Text(
        text = stringResource(text),
        style = TextStyle(
            color = HushColors.textBody,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            fontWeight = fontWeight,
            textAlign = TextAlign.Center
        )
    )
}