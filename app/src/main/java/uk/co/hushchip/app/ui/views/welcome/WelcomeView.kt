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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.ui.theme.outfitFamily
import uk.co.hushchip.app.utils.hushClickEffect

@Composable
fun WelcomeView(
    screenIndex: Int,
    heading: String,
    body: String,
    bodyColor: Color = HushColors.textMuted,
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
                .padding(horizontal = 20.dp)
        ) {
            // Top area with illustration - vertically centred
            Box(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                topContent()
            }

            // Heading
            Text(
                text = heading,
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 22.sp,
                    color = HushColors.textBright,
                    textAlign = TextAlign.Center
                )
            )

            // Body
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = body,
                modifier = Modifier.widthIn(max = 280.dp),
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp,
                    color = bodyColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
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
                            color = HushColors.danger,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = HushColors.dangerBg,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = it,
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = HushColors.danger,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.55f))

            // Dot indicator
            DotIndicator(currentIndex = screenIndex)

            Spacer(modifier = Modifier.height(16.dp))

            // Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(
                        color = HushColors.bgRaised,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = HushColors.border,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .hushClickEffect(onClick = onNext),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buttonText.uppercase(),
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        letterSpacing = 3.sp,
                        color = HushColors.textMuted
                    )
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
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
            if (i == currentIndex) {
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(HushColors.textMuted)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(HushColors.border)
                )
            }
            if (i < 2) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}
