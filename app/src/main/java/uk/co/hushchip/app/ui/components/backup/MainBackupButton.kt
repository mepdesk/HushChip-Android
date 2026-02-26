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
import uk.co.hushchip.app.R
import uk.co.hushchip.app.data.BackupStatus
import uk.co.hushchip.app.ui.components.shared.HushButton

@Composable
fun MainBackupButton(
    backupStatus: MutableState<BackupStatus>,
    onClick: () -> Unit
) {
    HushButton(
        onClick = {
            onClick()
        },
        text = when (backupStatus.value) {
            BackupStatus.DEFAULT -> {
                R.string.start
            }
            BackupStatus.FIRST_STEP -> {
                R.string.next
            }
            BackupStatus.SECOND_STEP -> {
                R.string.scanMySeedkeeper
            }
            BackupStatus.THIRD_STEP -> {
                R.string.makeBackup
            }
            BackupStatus.FOURTH_STEP -> {
                R.string.next
            }
            BackupStatus.SUCCESS -> {
                R.string.home
            }
            BackupStatus.FAILURE -> {
                R.string.home
            }
        }
    )
}