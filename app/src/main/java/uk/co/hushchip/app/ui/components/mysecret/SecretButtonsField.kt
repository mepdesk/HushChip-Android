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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.satochip.client.seedkeeper.SeedkeeperSecretType
import uk.co.hushchip.app.R
import uk.co.hushchip.app.data.MySecretStatus
import uk.co.hushchip.app.ui.components.shared.HushButton
import uk.co.hushchip.app.ui.theme.HushButtonPurple

@Composable
fun SecretButtonsField(
    mySecretStatus: MutableState<MySecretStatus>,
    isSecretShown: MutableState<Boolean>,
    type: String,
    subType: Int = 1,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val secretType = SeedkeeperSecretType.valueOf(type)
        when (secretType) {
            SeedkeeperSecretType.MASTERSEED, SeedkeeperSecretType.BIP39_MNEMONIC, SeedkeeperSecretType.ELECTRUM_MNEMONIC  -> {
                if (subType != 0 || secretType != SeedkeeperSecretType.MASTERSEED) {
                    HushButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            mySecretStatus.value = MySecretStatus.SEED
                        },
                        text = R.string.seed,
                        image = R.drawable.seed_icon,
                        horizontalPadding = 1.dp
                    )
                    HushButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onClick()
                            if (isSecretShown.value) {
                                mySecretStatus.value = MySecretStatus.SEED_QR
                            }
                        },
                        text = R.string.seedQR,
                        image = R.drawable.seedqr_icon,
                        horizontalPadding = 1.dp,
                        buttonColor = if (isSecretShown.value) HushButtonPurple else HushButtonPurple.copy(alpha = 0.6f)
                    )
                }
            }
            else -> {}
        }
    }
}