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

package uk.co.hushchip.app.ui.views.backup

import android.app.Activity
import android.content.Context
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import uk.co.hushchip.app.HomeView
import uk.co.hushchip.app.PinEntryView
import uk.co.hushchip.app.data.NfcActionType
import uk.co.hushchip.app.data.NfcResultCode
import uk.co.hushchip.app.data.PinCodeAction
import uk.co.hushchip.app.services.NFCCardService
import uk.co.hushchip.app.ui.components.backup.BackupErrorCard
import uk.co.hushchip.app.ui.components.shared.HeaderAlternateRow
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.ui.theme.outfitFamily
import uk.co.hushchip.app.utils.HapticUtil
import uk.co.hushchip.app.utils.hushClickEffect
import uk.co.hushchip.app.viewmodels.SharedViewModel

/**
 * Backup wizard step — UI-only state machine tracking which screen to show.
 * The actual backup logic is handled by NFCCardService via existing NfcActionType/NfcResultCode.
 */
private enum class BackupStep {
    SCAN_SOURCE,
    ENTER_SOURCE_PIN,
    READING_SOURCE,
    SOURCE_COMPLETE,
    SCAN_DESTINATION,
    ENTER_DEST_PIN,
    WRITING_DESTINATION,
    COMPLETE,
    ERROR
}

@Composable
fun BackupView(
    context: Context,
    navController: NavHostController,
    viewModel: SharedViewModel,
) {
    val view = LocalView.current

    // UI wizard step
    val step = remember { mutableStateOf(BackupStep.SCAN_SOURCE) }
    val errorMessage = remember { mutableStateOf("") }
    val showCancelDialog = remember { mutableStateOf(false) }

    // Protocol order: 1) Scan DESTINATION card (PIN + headers)
    //                  2) Hold SOURCE card (export secrets)
    //                  3) Hold DESTINATION card again (import secrets)

    // Reset result code on entry
    LaunchedEffect(Unit) {
        viewModel.setResultCodeLiveTo(NfcResultCode.NONE)
    }

    // React to NFC result codes to advance the wizard
    LaunchedEffect(viewModel.resultCodeLive) {
        when (viewModel.resultCodeLive) {
            NfcResultCode.BACKUP_CARD_SCANNED_SUCCESSFULLY -> {
                // Backup (destination) card scanned and PIN verified
                // secretHeadersForBackup now populated
                step.value = BackupStep.SOURCE_COMPLETE
            }
            NfcResultCode.SECRETS_EXPORTED_SUCCESSFULLY_FROM_MASTER -> {
                // Master (source) card secrets exported
                step.value = BackupStep.SCAN_DESTINATION
            }
            NfcResultCode.CARD_SUCCESSFULLY_BACKED_UP -> {
                step.value = BackupStep.COMPLETE
                HapticUtil.confirm(view)
            }
            NfcResultCode.CARD_BLOCKED -> {
                errorMessage.value = "Card is locked."
                step.value = BackupStep.ERROR
            }
            NfcResultCode.NO_MEMORY_LEFT -> {
                errorMessage.value = "No memory left on destination card."
                step.value = BackupStep.ERROR
            }
            NfcResultCode.WRONG_PIN -> {
                // Handled by NFC overlay — stays on current step
            }
            NfcResultCode.NFC_ERROR -> {
                // Card lost — handled by NFC overlay, stays on current step
            }
            else -> {}
        }
    }

    // Cancel confirmation dialog
    if (showCancelDialog.value) {
        AlertDialog(
            onDismissRequest = { showCancelDialog.value = false },
            containerColor = HushColors.bgRaised,
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "CANCEL BACKUP",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        letterSpacing = 3.sp,
                        color = HushColors.textBright
                    )
                )
            },
            text = {
                Text(
                    text = "Cancel backup? No data has been written to your destination card yet.",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 13.sp,
                        color = HushColors.textMuted,
                        lineHeight = 20.sp
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog.value = false
                    navController.popBackStack()
                }) {
                    Text(
                        text = "CANCEL BACKUP",
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp,
                            color = HushColors.danger
                        )
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog.value = false }) {
                    Text(
                        text = "CONTINUE",
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp,
                            color = HushColors.textMuted
                        )
                    )
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HushColors.bg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with back button
            HeaderAlternateRow(
                onClick = {
                    val inProgress = step.value != BackupStep.SCAN_SOURCE &&
                            step.value != BackupStep.COMPLETE &&
                            step.value != BackupStep.ERROR &&
                            step.value != BackupStep.SCAN_DESTINATION // haven't started writing yet
                    if (inProgress && step.value == BackupStep.WRITING_DESTINATION) {
                        // Don't allow cancel during write
                    } else if (step.value != BackupStep.SCAN_SOURCE &&
                        step.value != BackupStep.COMPLETE &&
                        step.value != BackupStep.ERROR
                    ) {
                        showCancelDialog.value = true
                    } else {
                        navController.popBackStack()
                    }
                },
                titleText = "Backup"
            )

            // Progress dots
            Spacer(modifier = Modifier.height(8.dp))
            BackupProgressDots(step = step.value)

            // Step content
            when (step.value) {
                BackupStep.SCAN_SOURCE -> ScanSourceStep(
                    onStartBackup = {
                        // Navigate to PIN entry for backup card
                        // The protocol needs the DESTINATION card first
                        // But we label it as "scan destination" from the user's perspective
                        step.value = BackupStep.ENTER_SOURCE_PIN
                        navController.navigate(
                            PinEntryView(
                                pinCodeAction = PinCodeAction.ENTER_PIN_CODE.name,
                                isBackupCard = true,
                            )
                        )
                    }
                )
                BackupStep.ENTER_SOURCE_PIN -> WaitingForPinStep()
                BackupStep.READING_SOURCE -> ReadingStep()
                BackupStep.SOURCE_COMPLETE -> SourceCompleteStep(
                    secretCount = NFCCardService.secretHeadersForBackup.size,
                    onProceed = {
                        // Now export from master card
                        step.value = BackupStep.READING_SOURCE
                        viewModel.showNfcOverlayForScan()
                        viewModel.scanCardForAction(
                            activity = context as Activity,
                            nfcActionType = NfcActionType.EXPORT_SECRETS_FROM_MASTER
                        )
                    }
                )
                BackupStep.SCAN_DESTINATION -> ScanDestinationStep(
                    onProceed = {
                        step.value = BackupStep.WRITING_DESTINATION
                        viewModel.showNfcOverlayForScan()
                        viewModel.scanCardForAction(
                            activity = context as Activity,
                            nfcActionType = NfcActionType.IMPORT_SECRETS_TO_BACKUP
                        )
                    }
                )
                BackupStep.ENTER_DEST_PIN -> WaitingForPinStep()
                BackupStep.WRITING_DESTINATION -> WritingStep(
                    progress = viewModel.backupImportProgress
                )
                BackupStep.COMPLETE -> CompleteStep(
                    secretCount = NFCCardService.backupNumberOfSecretsImported,
                    hasErrors = NFCCardService.backupErrors.isNotEmpty(),
                    onDone = {
                        navController.navigate(HomeView) {
                            popUpTo(0)
                        }
                    }
                )
                BackupStep.ERROR -> ErrorStep(
                    message = errorMessage.value,
                    onDone = {
                        navController.navigate(HomeView) {
                            popUpTo(0)
                        }
                    }
                )
            }
        }
    }
}

// ── Progress Dots ──────────────────────────────────────────────────────────────

@Composable
private fun BackupProgressDots(step: BackupStep) {
    val activeIndex = when (step) {
        BackupStep.SCAN_SOURCE -> 0
        BackupStep.ENTER_SOURCE_PIN -> 1
        BackupStep.READING_SOURCE -> 1
        BackupStep.SOURCE_COMPLETE -> 2
        BackupStep.SCAN_DESTINATION -> 2
        BackupStep.ENTER_DEST_PIN -> 2
        BackupStep.WRITING_DESTINATION -> 3
        BackupStep.COMPLETE -> 3
        BackupStep.ERROR -> -1
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0..3) {
            val isFilled = i <= activeIndex
            if (isFilled) {
                Canvas(
                    modifier = Modifier
                        .width(16.dp)
                        .height(8.dp)
                ) {
                    val dotWidth = 12.dp.toPx()
                    val dotHeight = 6.dp.toPx()
                    val left = (size.width - dotWidth) / 2f
                    val top = (size.height - dotHeight) / 2f
                    // Glow
                    drawRoundRect(
                        color = HushColors.textMuted.copy(alpha = 0.15f),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                        topLeft = Offset(left - 2.dp.toPx(), top - 2.dp.toPx()),
                        size = Size(dotWidth + 4.dp.toPx(), dotHeight + 4.dp.toPx())
                    )
                    // Dot
                    drawRoundRect(
                        color = HushColors.textMuted,
                        cornerRadius = CornerRadius(3.dp.toPx()),
                        topLeft = Offset(left, top),
                        size = Size(dotWidth, dotHeight)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(HushColors.border)
                )
            }
            if (i < 3) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

// ── NFC Animation (inline, reused from NfcScanOverlay pattern) ─────────────────

@Composable
private fun InlineNfcAnimation(
    modifier: Modifier = Modifier,
    isAnimating: Boolean = true,
    color: Color = HushColors.textFaint,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "backup_nfc")

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

    Box(
        modifier = modifier
            .size(120.dp)
            .background(Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(100.dp)) {
            val cx = size.width * 0.35f
            val cy = size.height * 0.5f

            // Phone outline
            val phoneW = size.width * 0.3f
            val phoneH = size.height * 0.6f
            val phoneLeft = cx - phoneW / 2f
            val phoneTop = cy - phoneH / 2f
            drawRoundRect(
                color = color,
                topLeft = Offset(phoneLeft, phoneTop),
                size = Size(phoneW, phoneH),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 1.5f)
            )
            drawCircle(
                color = color.copy(alpha = 0.5f),
                radius = 2.5f,
                center = Offset(cx, phoneTop + 10f)
            )

            // NFC arcs
            val arcOriginX = cx + phoneW / 2f + 6f
            val arcOriginY = cy
            val depthMultipliers = listOf(1.0f, 0.6f, 0.3f)

            if (isAnimating) {
                val alphas = listOf(arc1Alpha, arc2Alpha, arc3Alpha)
                for (i in 0..2) {
                    val radius = 14f + (i * 12f)
                    drawArc(
                        color = color.copy(alpha = alphas[i] * depthMultipliers[i]),
                        startAngle = -50f,
                        sweepAngle = 100f,
                        useCenter = false,
                        topLeft = Offset(arcOriginX - radius, arcOriginY - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(width = 1.5f)
                    )
                }
            } else {
                for (i in 0..2) {
                    val radius = 14f + (i * 12f)
                    drawArc(
                        color = color.copy(alpha = 0.2f * depthMultipliers[i]),
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
    }
}

// ── Pulsing Dots Loading Indicator ─────────────────────────────────────────────

@Composable
private fun PulsingDots(color: Color = HushColors.textFaint) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing_dots")

    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "dot1"
    )
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "dot2"
    )
    val dot3Alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "dot3"
    )

    Row(horizontalArrangement = Arrangement.Center) {
        listOf(dot1Alpha, dot2Alpha, dot3Alpha).forEach { alpha ->
            Canvas(modifier = Modifier.size(6.dp)) {
                drawCircle(color = color.copy(alpha = alpha))
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

// ── Step: Scan Source ──────────────────────────────────────────────────────────

@Composable
private fun ScanSourceStep(onStartBackup: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(0.15f))

        Text(
            text = "SCAN DESTINATION CARD",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                letterSpacing = 5.sp,
                color = HushColors.textBright
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Hold the empty card you want to\ncopy your secrets TO",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Light,
                fontSize = 13.sp,
                color = HushColors.textMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        InlineNfcAnimation()

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "WAITING FOR CARD...",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 9.sp,
                letterSpacing = 3.sp,
                color = HushColors.textGhost
            )
        )

        Spacer(modifier = Modifier.weight(0.4f))

        // Info box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(HushColors.bgSurface, RoundedCornerShape(12.dp))
                .border(1.dp, HushColors.border, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "The backup process will first scan your destination card, then ask for your source card to read secrets, then write them to the destination.",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 11.sp,
                    color = HushColors.textFaint,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Start button
        WizardButton(
            text = "START BACKUP",
            onClick = onStartBackup
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ── Step: Waiting for PIN (brief transition) ──────────────────────────────────

@Composable
private fun WaitingForPinStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "ENTERING PIN",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                letterSpacing = 5.sp,
                color = HushColors.textBright
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        PulsingDots()
    }
}

// ── Step: Reading Source ───────────────────────────────────────────────────────

@Composable
private fun ReadingStep() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "READING SECRETS",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                letterSpacing = 5.sp,
                color = HushColors.textBright
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        PulsingDots()
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Keep your card held steady...",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Light,
                fontSize = 12.sp,
                color = HushColors.textMuted,
                textAlign = TextAlign.Center
            )
        )
    }
}

// ── Step: Source Complete ──────────────────────────────────────────────────────

@Composable
private fun SourceCompleteStep(
    secretCount: Int,
    onProceed: () -> Unit,
) {
    val existingSecretCount = NFCCardService.backupSecretHeaders.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(0.15f))

        Text(
            text = "DESTINATION CARD SCANNED",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                letterSpacing = 5.sp,
                color = HushColors.textBright
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Checkmark
        Canvas(modifier = Modifier.size(40.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            drawCircle(
                color = HushColors.textMuted.copy(alpha = 0.1f),
                radius = size.minDimension / 2f
            )
            // Checkmark path
            val strokeWidth = 2.dp.toPx()
            drawLine(
                color = HushColors.textBright,
                start = Offset(cx - 8.dp.toPx(), cy),
                end = Offset(cx - 2.dp.toPx(), cy + 6.dp.toPx()),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = HushColors.textBright,
                start = Offset(cx - 2.dp.toPx(), cy + 6.dp.toPx()),
                end = Offset(cx + 8.dp.toPx(), cy - 4.dp.toPx()),
                strokeWidth = strokeWidth
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "$secretCount secrets to copy",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Light,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp,
                color = HushColors.textBody
            )
        )

        // Info banner if destination has existing secrets
        if (existingSecretCount > 0) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HushColors.bgSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, HushColors.border, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = "This card already contains $existingSecretCount secrets. Backup will ADD your secrets \u2014 it will not overwrite existing ones.",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 11.sp,
                        color = HushColors.textFaint,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider(
            color = HushColors.border.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Now hold your SOURCE card\n(the card with your secrets)\nto the back of your phone.",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Light,
                fontSize = 13.sp,
                color = HushColors.textMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        InlineNfcAnimation()

        Spacer(modifier = Modifier.weight(0.4f))

        WizardButton(
            text = "SCAN SOURCE CARD",
            onClick = onProceed
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ── Step: Scan Destination ────────────────────────────────────────────────────

@Composable
private fun ScanDestinationStep(
    onProceed: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(0.15f))

        Text(
            text = "SOURCE CARD READ",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                letterSpacing = 5.sp,
                color = HushColors.textBright
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Checkmark
        Canvas(modifier = Modifier.size(40.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            drawCircle(
                color = HushColors.textMuted.copy(alpha = 0.1f),
                radius = size.minDimension / 2f
            )
            val strokeWidth = 2.dp.toPx()
            drawLine(
                color = HushColors.textBright,
                start = Offset(cx - 8.dp.toPx(), cy),
                end = Offset(cx - 2.dp.toPx(), cy + 6.dp.toPx()),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = HushColors.textBright,
                start = Offset(cx - 2.dp.toPx(), cy + 6.dp.toPx()),
                end = Offset(cx + 8.dp.toPx(), cy - 4.dp.toPx()),
                strokeWidth = strokeWidth
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Secrets exported successfully",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                color = HushColors.textBody
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        HorizontalDivider(
            color = HushColors.border.copy(alpha = 0.5f),
            thickness = 0.5.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Remove your source card.\n\nNow hold your DESTINATION card\nto the back of your phone.",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Light,
                fontSize = 13.sp,
                color = HushColors.textMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        InlineNfcAnimation()

        Spacer(modifier = Modifier.weight(0.4f))

        WizardButton(
            text = "WRITE TO DESTINATION",
            onClick = onProceed
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ── Step: Writing Destination ─────────────────────────────────────────────────

@Composable
private fun WritingStep(progress: Float) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "WRITING SECRETS",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                letterSpacing = 5.sp,
                color = HushColors.textBright
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        PulsingDots()

        if (progress > 0f) {
            Spacer(modifier = Modifier.height(16.dp))
            // Progress bar
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
                            HushColors.textMuted.copy(alpha = 0.5f),
                            RoundedCornerShape(1.dp)
                        )
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${(progress * 100).toInt()}%",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                    color = HushColors.textGhost
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Do not remove your card",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = HushColors.danger,
                textAlign = TextAlign.Center
            )
        )
    }
}

// ── Step: Complete ────────────────────────────────────────────────────────────

@Composable
private fun CompleteStep(
    secretCount: Int,
    hasErrors: Boolean,
    onDone: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "BACKUP COMPLETE",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                letterSpacing = 5.sp,
                color = HushColors.textBright
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Large checkmark
        Canvas(modifier = Modifier.size(64.dp)) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            // Glow
            drawCircle(
                color = HushColors.textMuted.copy(alpha = 0.08f),
                radius = size.minDimension / 2f + 4.dp.toPx()
            )
            drawCircle(
                color = HushColors.textMuted.copy(alpha = 0.12f),
                radius = size.minDimension / 2f
            )
            val strokeWidth = 3.dp.toPx()
            drawLine(
                color = HushColors.textBright,
                start = Offset(cx - 12.dp.toPx(), cy + 1.dp.toPx()),
                end = Offset(cx - 3.dp.toPx(), cy + 10.dp.toPx()),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = HushColors.textBright,
                start = Offset(cx - 3.dp.toPx(), cy + 10.dp.toPx()),
                end = Offset(cx + 12.dp.toPx(), cy - 6.dp.toPx()),
                strokeWidth = strokeWidth
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "$secretCount secrets copied successfully",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Light,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp,
                color = HushColors.textBody
            )
        )

        // Show errors if any
        if (hasErrors) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SOME SECRETS COULD NOT BE COPIED",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 9.sp,
                    letterSpacing = 3.sp,
                    color = HushColors.danger
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            NFCCardService.backupErrors.forEach { errorItem ->
                BackupErrorCard(errorItem)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        WizardButton(
            text = "DONE",
            onClick = onDone
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// ── Step: Error ───────────────────────────────────────────────────────────────

@Composable
private fun ErrorStep(
    message: String,
    onDone: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "BACKUP FAILED",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                letterSpacing = 5.sp,
                color = HushColors.danger
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = message,
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                color = HushColors.textMuted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        WizardButton(
            text = "DONE",
            onClick = onDone
        )
    }
}

// ── Shared Wizard Button ──────────────────────────────────────────────────────

@Composable
private fun WizardButton(
    text: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF161618),
                        Color(0xFF0E0E10),
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.06f),
                        Color.White.copy(alpha = 0.02f),
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .hushClickEffect(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                letterSpacing = 3.sp,
                color = HushColors.textMuted
            )
        )
    }
}
