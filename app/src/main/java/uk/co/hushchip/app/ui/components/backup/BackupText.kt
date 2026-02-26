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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import uk.co.hushchip.app.R
import uk.co.hushchip.app.data.BackupStatus

@Composable
fun BackupText(
    backupStatus: MutableState<BackupStatus>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        when (backupStatus.value) {
            BackupStatus.DEFAULT -> {
                BackupSingleText(R.string.backupDescription)
                Spacer(modifier = Modifier.height(16.dp))
                BackupSingleText(R.string.pairingStartProcess)
            }
            BackupStatus.FIRST_STEP -> {
                BackupSingleText(R.string.pairingFirstStep)
            }
            BackupStatus.SECOND_STEP -> {
                BackupSingleText(R.string.pairingSecondStep)
            }
            BackupStatus.THIRD_STEP -> {
                BackupSingleText(R.string.pairingThirdStepDoneTitle)
                Spacer(modifier = Modifier.height(16.dp))
                BackupSingleText(R.string.pairingThirdStepDoneText)
            }
            BackupStatus.FOURTH_STEP -> {
                BackupSingleText(R.string.pairingFourthStep)
            }
            BackupStatus.SUCCESS -> {
                BackupSingleText(R.string.congratulations, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                BackupSingleText(R.string.backupFifthText)
            }
            BackupStatus.FAILURE -> {
                BackupSingleText(R.string.backupWarning, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                BackupSingleText(R.string.backupFailedText)
            }
        }
    }
}