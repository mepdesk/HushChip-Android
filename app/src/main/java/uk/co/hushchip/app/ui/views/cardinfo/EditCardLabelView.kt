package uk.co.hushchip.app.ui.views.cardinfo

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import uk.co.hushchip.app.R
import uk.co.hushchip.app.data.AppErrorMsg
import uk.co.hushchip.app.data.NfcActionType
import uk.co.hushchip.app.data.NfcResultCode
import uk.co.hushchip.app.services.HushLog
import uk.co.hushchip.app.ui.components.import.InputField
import uk.co.hushchip.app.ui.components.home.NfcDialog
import uk.co.hushchip.app.ui.components.shared.HeaderAlternateRow
import uk.co.hushchip.app.ui.components.shared.HushButton
import uk.co.hushchip.app.ui.components.shared.TitleTextField
import uk.co.hushchip.app.ui.theme.HushButtonPurple
import uk.co.hushchip.app.ui.theme.SatoPurple
import uk.co.hushchip.app.viewmodels.SharedViewModel

@Composable
fun EditCardLabelView(
    context: Context,
    navController: NavHostController,
    viewModel: SharedViewModel,
) {
    // NFC dialog
    val showNfcDialog = remember { mutableStateOf(false) } // for NfcDialog
    if (showNfcDialog.value) {
        NfcDialog(
            openDialogCustom = showNfcDialog,
            resultCodeLive = viewModel.resultCodeLive,
            isConnected = viewModel.isCardConnected
        )
    }

    val cardLabelToast = stringResource(id = R.string.cardLabelToast)
    LaunchedEffect(viewModel.resultCodeLive) {
        if (viewModel.resultCodeLive == NfcResultCode.CARD_LABEL_CHANGED_SUCCESSFULLY) {
            Toast.makeText(context, cardLabelToast, Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }
    LaunchedEffect(Unit) {
        HushLog.d("EditCardLabelView", "EditCardLabelView DEBUG DEBUG")
    }

    // error mgmt
    val showError = remember {
        mutableStateOf(false)
    }
    val appError = remember {
        mutableStateOf(AppErrorMsg.OK)
    }

    // label field
    val cardLabel = viewModel.cardLabel
    val curValueLabel = remember {
        mutableStateOf(cardLabel)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderAlternateRow(
                onClick = {
                    navController.popBackStack()
                },
                titleText = stringResource(R.string.blankTextField)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 16.dp, top = 16.dp),
                //.verticalScroll(state = scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // TITLE
                TitleTextField(
                    title = R.string.cardLabelTitle,
                    text = R.string.cardLabelMsg
                )
                Spacer(modifier = Modifier.height(8.dp))

                // LABEL
                InputField(
                    curValue = curValueLabel,
                    placeHolder = R.string.label,
                    containerColor = SatoPurple.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(12.dp))

//    }
//
//    Column(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {

                // error msg
                if (showError.value) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(appError.value.msg),
                        style = TextStyle(
                            color = Color.Red,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Edit button
                    HushButton(
                        modifier = Modifier,
                        text = R.string.importButton,
                        onClick = {
                            //check inputs
                            if (curValueLabel.value.toByteArray(Charsets.UTF_8).size > 64) {
                                appError.value = AppErrorMsg.CARD_LABEL_TOO_LONG
                                showError.value = true
                                return@HushButton
                            }

                            // send command to card
                            showNfcDialog.value = true
                            viewModel.setNewCardLabel(curValueLabel.value)
                            viewModel.scanCardForAction(
                                activity = context as Activity,
                                nfcActionType = NfcActionType.EDIT_CARD_LABEL
                            )

                        },
                        buttonColor = if (curValueLabel.value.isNotEmpty())
                            HushButtonPurple else HushButtonPurple.copy(alpha = 0.6f),
                    ) // import button
                } // Row
            }
        }
    }
}