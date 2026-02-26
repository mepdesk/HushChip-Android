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

package uk.co.hushchip.app.ui.views.welcome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.ui.theme.outfitFamily

@Composable
fun WelcomeView(
    screenIndex: Int,
    heading: String,
    body: String,
    bodyColor: Color = HushColors.textFaint,
    buttonText: String,
    warningText: String? = null,
    topContent: @Composable () -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HushColors.bg)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    when {
                        dragAmount > 0 -> onBack()
                        dragAmount < 0 -> onNext()
                    }
                    change.consume()
                }
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            // Top 40% area with icon
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                topContent()
            }

            // Heading
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = heading,
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 20.sp,
                    color = HushColors.textBright,
                    textAlign = TextAlign.Center
                )
            )

            // Body
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = body,
                modifier = Modifier.widthIn(max = 280.dp),
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 13.sp,
                    color = bodyColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            )

            // Warning box (screen 3 only)
            warningText?.let {
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = HushColors.dangerBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .background(
                            color = HushColors.dangerBg,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = it,
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = HushColors.danger,
                            textAlign = TextAlign.Center,
                            letterSpacing = 0.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.6f))

            // Button
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonColors(
                    containerColor = HushColors.border,
                    contentColor = HushColors.textBright,
                    disabledContainerColor = HushColors.border,
                    disabledContentColor = HushColors.textBright
                )
            ) {
                Text(
                    text = buttonText.uppercase(),
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        letterSpacing = 4.sp,
                        color = HushColors.textBright
                    )
                )
            }

            // Dot indicator
            Spacer(modifier = Modifier.height(24.dp))
            DotIndicator(currentIndex = screenIndex)

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun DotIndicator(currentIndex: Int) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0..2) {
            Canvas(modifier = Modifier.size(6.dp)) {
                drawCircle(
                    color = if (i == currentIndex) HushColors.textMuted else HushColors.border
                )
            }
            if (i < 2) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}
