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

package uk.co.hushchip.app.ui.components.import

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import uk.co.hushchip.app.ui.theme.SatoChecked
import uk.co.hushchip.app.ui.theme.SatoChecker

@Composable
fun ToggleOption(
    modifier: Modifier = Modifier,
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White
        )
        Switch(
            modifier = Modifier
                .scale(scale = 0.8f),
            checked = isChecked,
            onCheckedChange = { onCheckedChange(it) },
            colors = SwitchDefaults.colors(
                checkedThumbColor = SatoChecked,
                checkedTrackColor = Color.White,
                uncheckedThumbColor = SatoChecker,
                uncheckedTrackColor = Color.White,
                checkedBorderColor = Color.Transparent,
                disabledUncheckedBorderColor = Color.Transparent,
                disabledCheckedBorderColor = Color.Transparent,
                uncheckedBorderColor = Color.Transparent,
            ),
            thumbContent = {
                Box(
                    modifier = Modifier
                    .size(16.dp)
                    .background(
                        shape = CircleShape,
                        color = if (isChecked) SatoChecked else SatoChecker
                    )
                )
            }
        )
    }
}