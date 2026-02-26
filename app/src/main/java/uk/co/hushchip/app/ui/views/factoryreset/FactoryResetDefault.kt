package uk.co.hushchip.app.ui.views.factoryreset

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import uk.co.hushchip.app.ui.theme.HushColors
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import uk.co.hushchip.app.R
import uk.co.hushchip.app.data.FactoryResetStatus
import uk.co.hushchip.app.data.NfcActionType
import uk.co.hushchip.app.ui.components.settings.CardResetButton
import uk.co.hushchip.app.ui.components.settings.ResetCardTextField
import uk.co.hushchip.app.ui.components.shared.HushButton
import uk.co.hushchip.app.utils.HapticUtil
import uk.co.hushchip.app.viewmodels.SharedViewModel

@Composable
fun FactoryResetDefault(
    context: Context,
    navController: NavHostController,
    viewModel: SharedViewModel,
    factoryResetStatus: MutableState<FactoryResetStatus>,
) {
    val view = LocalView.current
    val isChecked = remember {
        mutableStateOf(false)
    }

    ResetCardTextField(
        text = R.string.factoryResetWarningText,
        warning = R.string.allDataErasedWarning,
        subText = R.string.allDataErasedIrreversible,
    )

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isChecked.value,
            onCheckedChange = {
                isChecked.value = it
            }
        )
        Text(
            modifier = Modifier
                .clickable { isChecked.value = !isChecked.value },
            text = stringResource(id = R.string.checkToProceed),
            style = TextStyle(
                color = HushColors.textBody,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraLight
            )
        )
    }
    Spacer(modifier = Modifier.height(35.dp))
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CardResetButton(
            text = stringResource(id = R.string.resetMyCard),
            onClick = {
                if (isChecked.value) {
                    HapticUtil.heavy(view)
                    factoryResetStatus.value = FactoryResetStatus.RESET_READY
                }
            },
            containerColor = if (isChecked.value) HushColors.danger else HushColors.danger.copy(0.6f),
        )
        Spacer(modifier = Modifier.height(12.dp))
        HushButton(
            modifier = Modifier
                .padding(
                    horizontal = 6.dp
                ),
            onClick = {
                factoryResetStatus.value = FactoryResetStatus.RESET_CANCELLED
            },
            text = R.string.cancel,
        )
        Spacer(modifier = Modifier.height(35.dp))

    }
}