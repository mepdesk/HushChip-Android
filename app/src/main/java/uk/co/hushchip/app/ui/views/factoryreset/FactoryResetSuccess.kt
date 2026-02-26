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

package uk.co.hushchip.app.ui.views.factoryreset

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import uk.co.hushchip.app.HomeView
import uk.co.hushchip.app.R
import uk.co.hushchip.app.ui.components.settings.ResetCardTextField
import uk.co.hushchip.app.ui.components.shared.IllustrationPlaceholder
import uk.co.hushchip.app.ui.components.shared.HushButton
import uk.co.hushchip.app.viewmodels.SharedViewModel

@Composable
fun FactoryResetSuccess(
    context: Context,
    navController: NavHostController,
    viewModel: SharedViewModel,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ResetCardTextField(
            title = R.string.congratulations,
            text = R.string.resetSuccessful,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            IllustrationPlaceholder(
                modifier = Modifier.size(200.dp),
                label = "Card reset"
            )
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HushButton(
            modifier = Modifier
                .padding(
                    horizontal = 6.dp
                ),
            onClick = {
                navController.navigate(HomeView) {
                    popUpTo(0)
                }
            },
            text = R.string.home,
        )
        Spacer(modifier = Modifier.height(35.dp))
    }
}