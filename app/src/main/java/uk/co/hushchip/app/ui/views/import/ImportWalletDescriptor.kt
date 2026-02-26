package uk.co.hushchip.app.ui.views.import

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import org.satochip.client.seedkeeper.SeedkeeperSecretType
import uk.co.hushchip.app.R
import uk.co.hushchip.app.data.AppErrorMsg
import uk.co.hushchip.app.data.ImportMode
import uk.co.hushchip.app.data.NfcActionType
import uk.co.hushchip.app.data.SecretData
import uk.co.hushchip.app.ui.components.import.InputField
import uk.co.hushchip.app.ui.components.import.SecretTextField
import uk.co.hushchip.app.ui.components.shared.HushButton
import uk.co.hushchip.app.ui.components.shared.TitleTextField
import uk.co.hushchip.app.ui.theme.HushButtonPurple
import uk.co.hushchip.app.ui.theme.SatoPurple
import uk.co.hushchip.app.utils.isClickable
import uk.co.hushchip.app.viewmodels.SharedViewModel

@Composable
fun ImportWalletDescriptor(
    context: Context,
    navController: NavHostController,
    viewModel: SharedViewModel,
    curValueLabel: MutableState<String>,
) {
    // error mgmt
    val showError = remember {
        mutableStateOf(false)
    }
    val appError = remember {
        mutableStateOf(AppErrorMsg.OK)
    }

    // secret fields
    val secret = remember {
        mutableStateOf("")
    }

    TitleTextField(
        title = R.string.importAWalletDescriptor,
        text = R.string.importAWalletDescriptorMessage
    )

    InputField(
        curValue = curValueLabel,
        placeHolder = R.string.label,
        containerColor = SatoPurple.copy(alpha = 0.5f)
    )

    SecretTextField(
        curValue = secret,
        placeholder = stringResource(id = R.string.enterYourWalletDescriptor),
        isEditable = true,
        isQRCodeEnabled = false,
        isSeedQRCodeEnabled = false,
        minHeight = 250.dp
    )


    // error msg
    if (showError.value) {
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
        //Import
        HushButton(
            modifier = Modifier,
            onClick = {
                //check inputs
                if (curValueLabel.value.isEmpty()){
                    appError.value = AppErrorMsg.LABEL_EMPTY
                    showError.value = true
                    return@HushButton
                }
                if (curValueLabel.value.toByteArray(Charsets.UTF_8).size > 127){
                    appError.value = AppErrorMsg.LABEL_TOO_LONG
                    showError.value = true
                    return@HushButton
                }
                if (secret.value.isEmpty()){
                    appError.value = AppErrorMsg.DESCRIPTOR_EMPTY
                    showError.value = true
                    return@HushButton
                }
                if (secret.value.toByteArray(Charsets.UTF_8).size > 65535){
                    appError.value = AppErrorMsg.DESCRIPTOR_TOO_LONG
                    showError.value = true
                    return@HushButton
                }

                val secretData = SecretData(
                    type = SeedkeeperSecretType.WALLET_DESCRIPTOR,
                    label = curValueLabel.value,
                    descriptor = secret.value
                )

                if (viewModel.getProtocolVersionInt() == 1){
                    val payloadBytes = secretData.getSecretBytes()
                    if (payloadBytes.size > 255){
                        appError.value = AppErrorMsg.SECRET_TOO_LONG_FOR_V1
                        showError.value = true
                        return@HushButton
                    }
                }

                viewModel.setSecretData(secretData)
                viewModel.showNfcOverlayForScan()
                viewModel.scanCardForAction(
                    activity = context as Activity,
                    nfcActionType = NfcActionType.IMPORT_SECRET
                )
            },
            text = R.string.importButton,
            buttonColor = if (
                isClickable(
                    secret,
                    curValueLabel
                )
            ) HushButtonPurple else HushButtonPurple.copy(alpha = 0.6f),
        )
    }
}