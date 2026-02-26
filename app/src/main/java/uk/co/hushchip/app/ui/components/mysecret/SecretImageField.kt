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

package uk.co.hushchip.app.ui.components.mysecret

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.g0dkar.qrcode.QRCode
import uk.co.hushchip.app.ui.components.shared.DataAsQrCode
import uk.co.hushchip.app.ui.theme.SatoDividerPurple

@Composable
fun SecretImageField(
    minHeight: Dp = 150.dp,
    qrCodeString: MutableState<String>,
    containerColor: Color = SatoDividerPurple.copy(alpha = 0.2f),
) {
    Box(
        modifier = Modifier
            .heightIn(
                min = minHeight
            ).fillMaxWidth()
            .background(
                color = containerColor,
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (qrCodeString.value.isNotEmpty()) {
            val qrCodeBytes = QRCode(qrCodeString.value).render().getBytes()
            DataAsQrCode(
                qrCodeBytes = qrCodeBytes
            )
        }
    }
}