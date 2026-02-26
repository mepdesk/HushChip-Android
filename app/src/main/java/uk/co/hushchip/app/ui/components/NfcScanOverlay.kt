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

package uk.co.hushchip.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.ui.theme.outfitFamily

sealed class NfcScanStatus {
    data object WaitingForCard : NfcScanStatus()
    data object Reading : NfcScanStatus()
    data object VerifyingPin : NfcScanStatus()
    data object WritingSecret : NfcScanStatus()
    data class Error(val message: String) : NfcScanStatus()
    data object Success : NfcScanStatus()
}

@Composable
fun NfcScanOverlay(
    isVisible: Boolean,
    status: NfcScanStatus,
    onCancel: () -> Unit,
    onSuccessDismiss: () -> Unit = {},
    progress: Float? = null,
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(200)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = HushColors.bgRaised,
                border = BorderStroke(1.dp, HushColors.border),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .padding(horizontal = 40.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // NFC animation
                    NfcIconAnimation(status = status)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Status text
                    val (statusText, statusColor) = when (status) {
                        is NfcScanStatus.WaitingForCard -> "HOLD YOUR CARD TO THE\nBACK OF YOUR PHONE" to HushColors.textMuted
                        is NfcScanStatus.Reading -> "READING..." to HushColors.textFaint
                        is NfcScanStatus.VerifyingPin -> "VERIFYING PIN..." to HushColors.textFaint
                        is NfcScanStatus.WritingSecret -> "SAVING SECRET..." to HushColors.textFaint
                        is NfcScanStatus.Error -> status.message.uppercase() to HushColors.danger
                        is NfcScanStatus.Success -> "DONE" to HushColors.textMuted
                    }

                    Text(
                        text = statusText,
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 9.sp,
                            letterSpacing = 3.sp,
                            color = statusColor,
                            textAlign = TextAlign.Center
                        )
                    )

                    // Progress indicator
                    if (progress != null && (status is NfcScanStatus.Reading || status is NfcScanStatus.WritingSecret)) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(HushColors.border, RoundedCornerShape(1.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = progress.coerceIn(0f, 1f))
                                    .height(2.dp)
                                    .background(
                                        Color(0xFFB4975A).copy(alpha = 0.4f),
                                        RoundedCornerShape(1.dp)
                                    )
                            )
                        }
                    }

                    // Cancel button — only during WaitingForCard
                    if (status is NfcScanStatus.WaitingForCard) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(HushColors.border)
                        )
                        TextButton(
                            onClick = onCancel,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "CANCEL",
                                style = TextStyle(
                                    fontFamily = outfitFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 10.sp,
                                    letterSpacing = 2.sp,
                                    color = HushColors.textFaint
                                )
                            )
                        }
                    }

                    // Auto-dismiss on success
                    if (status is NfcScanStatus.Success) {
                        LaunchedEffect(Unit) {
                            delay(500)
                            onSuccessDismiss()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NfcIconAnimation(status: NfcScanStatus) {
    val infiniteTransition = rememberInfiniteTransition(label = "nfc_pulse")

    val arc1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arc1"
    )
    val arc2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arc2"
    )
    val arc3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arc3"
    )

    val isAnimating = status is NfcScanStatus.WaitingForCard ||
            status is NfcScanStatus.Reading ||
            status is NfcScanStatus.VerifyingPin ||
            status is NfcScanStatus.WritingSecret

    val phoneColor = when (status) {
        is NfcScanStatus.Error -> HushColors.danger
        is NfcScanStatus.Success -> Color(0xFFB4975A)
        else -> HushColors.textMuted
    }

    val arcColor = when (status) {
        is NfcScanStatus.Error -> HushColors.danger
        is NfcScanStatus.Success -> Color(0xFFB4975A)
        else -> HushColors.textFaint
    }

    Box(
        modifier = Modifier
            .size(80.dp)
            .drawBehind {
                val cx = size.width * 0.35f
                val cy = size.height * 0.5f

                // Phone outline
                val phoneW = size.width * 0.35f
                val phoneH = size.height * 0.7f
                val phoneLeft = cx - phoneW / 2f
                val phoneTop = cy - phoneH / 2f
                drawRoundRect(
                    color = phoneColor,
                    topLeft = Offset(phoneLeft, phoneTop),
                    size = Size(phoneW, phoneH),
                    cornerRadius = CornerRadius(6f, 6f),
                    style = Stroke(width = 1.5f)
                )
                // Camera dot
                drawCircle(
                    color = phoneColor.copy(alpha = 0.5f),
                    radius = 2f,
                    center = Offset(cx, phoneTop + 8f)
                )

                // NFC arcs radiating from phone
                val arcOriginX = cx + phoneW / 2f + 4f
                val arcOriginY = cy

                if (isAnimating) {
                    val alphas = listOf(arc1Alpha, arc2Alpha, arc3Alpha)
                    for (i in 0..2) {
                        val radius = 12f + (i * 10f)
                        drawArc(
                            color = arcColor.copy(alpha = alphas[i]),
                            startAngle = -50f,
                            sweepAngle = 100f,
                            useCenter = false,
                            topLeft = Offset(arcOriginX - radius, arcOriginY - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = 1.5f)
                        )
                    }
                } else {
                    // Static arcs for error/success
                    for (i in 0..2) {
                        val radius = 12f + (i * 10f)
                        drawArc(
                            color = arcColor.copy(alpha = 0.3f),
                            startAngle = -50f,
                            sweepAngle = 100f,
                            useCenter = false,
                            topLeft = Offset(arcOriginX - radius, arcOriginY - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(width = 1.5f)
                        )
                    }
                }
            }
    )
}
