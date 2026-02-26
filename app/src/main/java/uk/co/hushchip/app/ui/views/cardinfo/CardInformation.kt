package uk.co.hushchip.app.ui.views.cardinfo

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import uk.co.hushchip.app.EditCardLabelView
import uk.co.hushchip.app.PinEntryView
import uk.co.hushchip.app.R
import uk.co.hushchip.app.ShowCardLogs
import uk.co.hushchip.app.data.NfcResultCode
import uk.co.hushchip.app.data.PinCodeAction
import uk.co.hushchip.app.ui.components.card.CardStatusField
import uk.co.hushchip.app.ui.components.card.InfoField
import uk.co.hushchip.app.ui.components.shared.HeaderAlternateRow
import uk.co.hushchip.app.ui.theme.SatoLightPurple
import uk.co.hushchip.app.viewmodels.SharedViewModel

@Composable
fun CardInformation(
    context: Context,
    navController: NavHostController,
    viewModel: SharedViewModel,
) {

    val cardLabel = viewModel.cardLabel
    val cardAppletVersion = viewModel.getAppletVersionString()
    val seedkeeperStatus = viewModel.getSeedkeeperStatus()
    val cardAuthentikey = viewModel.getAuthentikeyDescription()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderAlternateRow(
                titleText = stringResource(R.string.cardInfo),
                onClick = {
                    navController.popBackStack()
                }
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    CardStatusField(
                        title = R.string.seedkeeperStatus,
                        cardAppletVersion = cardAppletVersion,
                        cardAuthentikey = cardAuthentikey,
                        seedkeeperStatus = seedkeeperStatus
                    )
                    //Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    InfoField(
                        title = R.string.cardLabel,
                        text = cardLabel,
                        onClick = {
                            viewModel.setResultCodeLiveTo(NfcResultCode.NONE)
                            navController.navigate(EditCardLabelView)
                        },
                        containerColor = SatoLightPurple,
                        isClickable = true,
                        icon = R.drawable.edit_icon,
                        isPadded = false
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
                item {
                    InfoField(
                        title = R.string.pinCode,
                        text = stringResource(id = R.string.changePinCode),
                        onClick = {
                            viewModel.setResultCodeLiveTo(NfcResultCode.NONE)
                            navController.navigate(
                                PinEntryView(
                                    pinCodeAction = PinCodeAction.CHANGE_PIN_CODE.name,
                                    isBackupCard = true,
                                )
                            )
                        },
                        containerColor = SatoLightPurple,
                        isClickable = true,
                        icon = R.drawable.edit_icon,
                        isPadded = false
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
                item {
                    InfoField(
                        title = R.string.cardLogs,
                        text = stringResource(id = R.string.showCardLogs),
                        onClick = {
                            navController.navigate(ShowCardLogs)
                        },
                        containerColor = SatoLightPurple,
                        isClickable = true,
                        icon = R.drawable.free_data,
                        isPadded = false
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

        }
    }
}