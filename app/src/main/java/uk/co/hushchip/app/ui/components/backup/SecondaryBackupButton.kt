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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import uk.co.hushchip.app.R
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.data.BackupStatus
import uk.co.hushchip.app.ui.components.shared.HushButton

@Composable
fun SecondaryBackupButton(
    backupStatus: MutableState<BackupStatus>,
    goBack: () -> Unit,
) {
    HushButton(
        onClick = {
            goBack()
        },
        buttonColor = Color.Transparent,
        textColor = HushColors.textBody,
        text = when (backupStatus.value) {
            BackupStatus.FIRST_STEP -> {
                R.string.back
            }
            else -> {
                R.string.restart
            }
        }
    )
}