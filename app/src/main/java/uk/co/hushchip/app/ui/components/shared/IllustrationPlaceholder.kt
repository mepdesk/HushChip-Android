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

package uk.co.hushchip.app.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.hushchip.app.ui.theme.HushColors

@Composable
fun IllustrationPlaceholder(
    modifier: Modifier = Modifier,
    label: String = "",
) {
    Box(
        modifier = modifier
            .background(
                color = HushColors.bgRaised,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = TextStyle(
                    color = HushColors.textFaint,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}
