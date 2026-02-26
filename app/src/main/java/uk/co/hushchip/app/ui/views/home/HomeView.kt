package uk.co.hushchip.app.ui.views.home

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import uk.co.hushchip.app.AddSecretView
import uk.co.hushchip.app.MySecretView
import uk.co.hushchip.app.PinEntryView
import uk.co.hushchip.app.R
import uk.co.hushchip.app.data.NfcResultCode
import uk.co.hushchip.app.data.PinCodeAction
import uk.co.hushchip.app.ui.components.home.HomeHeaderRow
import uk.co.hushchip.app.ui.components.home.SatoGradientButton
import uk.co.hushchip.app.ui.components.home.HushRoundButton
import uk.co.hushchip.app.utils.webviewActivityIntent
import uk.co.hushchip.app.viewmodels.SharedViewModel

@Composable
fun HomeView(
    context: Context,
    navController: NavHostController,
    viewModel: SharedViewModel,
) {
    val buySeedkeeperUrl = stringResource(id = R.string.buySeedkeeperUrl)
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HomeHeaderRow(
                context = context,
                navController = navController,
                viewModel = viewModel,
            )
            if (viewModel.isCardDataAvailable) {
                SecretsList(
                    cardLabel = viewModel.cardLabel,
                    secretHeaders = viewModel.secretHeaders,
                    addNewSecret = {
                        navController.navigate(AddSecretView)
                    },
                    onSecretClick = { secretHeader ->
                        secretHeader.let {
                            viewModel.updateCurrentSecretHeader(secretHeader)
                            navController.navigate(MySecretView)
                        }
                    },
                )
            } else {
                // SCAN BUTTON
                HushRoundButton(
                    text = R.string.scan
                ) {
                    viewModel.setResultCodeLiveTo(NfcResultCode.NONE)
                    navController.navigate(
                        PinEntryView(
                            pinCodeAction = PinCodeAction.ENTER_PIN_CODE.name,
                            isBackupCard = false,
                        )
                    )
                }
                // WEBVIEW
                SatoGradientButton(
                    onClick = {
                        webviewActivityIntent(
                            url = buySeedkeeperUrl,
                            context = context
                        )
                    },
                    text = R.string.noSeedkeeper
                )
            }
        }
    }
}