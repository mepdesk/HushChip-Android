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

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import io.github.g0dkar.qrcode.QRCode

@Composable
fun DataAsQrCode(qrCodeBytes: ByteArray) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(vertical = 34.dp, horizontal = 16.dp)
    ) {
        val bitmapQrCode = BitmapFactory.decodeByteArray(qrCodeBytes, 0, qrCodeBytes.size)
        Image(
            modifier = Modifier
                .background(Color.White)
                .padding(5.dp),
            bitmap = bitmapQrCode.asImageBitmap(),
            contentDescription = null,
        )
    }
}