package uk.co.hushchip.app.ui.views.pincode

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
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
import uk.co.hushchip.app.data.PinCodeAction
import uk.co.hushchip.app.services.HushLog
import uk.co.hushchip.app.ui.components.shared.HeaderAlternateRow
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.ui.theme.outfitFamily
import uk.co.hushchip.app.utils.hushClickEffect
import uk.co.hushchip.app.viewmodels.SharedViewModel

@Composable
fun PinEntryView(
    context: Context,
    navController: NavHostController,
    viewModel: SharedViewModel,
    pinCodeAction: PinCodeAction,
    isBackupCard: Boolean,
) {
    LaunchedEffect(viewModel.resultCodeLive) {
        HushLog.d("PinEntryView", "LaunchedEffect resultCodeLive: ${viewModel.resultCodeLive}")

        when (pinCodeAction) {
            PinCodeAction.ENTER_PIN_CODE -> {
                if (viewModel.resultCodeLive == NfcResultCode.CARD_SCANNED_SUCCESSFULLY) {
                    navController.popBackStack()
                } else if (viewModel.resultCodeLive == NfcResultCode.BACKUP_CARD_SCANNED_SUCCESSFULLY) {
                    navController.popBackStack()
                }
            }
            PinCodeAction.SETUP_PIN_CODE -> {
                if (viewModel.resultCodeLive == NfcResultCode.CARD_SETUP_SUCCESSFUL) {
                    navController.popBackStack()
                } else if (viewModel.resultCodeLive == NfcResultCode.CARD_SETUP_FOR_BACKUP_SUCCESSFUL) {
                    navController.popBackStack()
                }
            }
            PinCodeAction.CHANGE_PIN_CODE -> {
                if (viewModel.resultCodeLive == NfcResultCode.PIN_CHANGED) {
                    navController.popBackStack()
                }
            }
            else -> {}
        }
    }

    val showError = remember { mutableStateOf(false) }
    val appError = remember { mutableStateOf(AppErrorMsg.OK) }

    val curPinValue = remember { mutableStateOf("") }
    val curSetupPinValue = remember { mutableStateOf("") }
    val curChangePinValue = remember { mutableStateOf("") }
    val curConfirmPinValue = remember { mutableStateOf("") }

    val pinCodeStatus = remember {
        mutableStateOf(
            when (pinCodeAction) {
                PinCodeAction.ENTER_PIN_CODE -> PinCodeAction.ENTER_PIN_CODE
                PinCodeAction.SETUP_PIN_CODE -> PinCodeAction.SETUP_PIN_CODE
                PinCodeAction.CHANGE_PIN_CODE -> PinCodeAction.ENTER_PIN_CODE
                else -> PinCodeAction.ENTER_PIN_CODE
            }
        )
    }

    val activePinValue = when (pinCodeStatus.value) {
        PinCodeAction.ENTER_PIN_CODE -> curPinValue
        PinCodeAction.SETUP_PIN_CODE -> curSetupPinValue
        PinCodeAction.CHANGE_PIN_CODE -> curChangePinValue
        PinCodeAction.CONFIRM_PIN_CODE -> curConfirmPinValue
        else -> curPinValue
    }

    fun checkPinFormat(pin: String): Boolean {
        if (pin.toByteArray(Charsets.UTF_8).size !in 4..16) {
            appError.value = AppErrorMsg.PIN_WRONG_FORMAT
            showError.value = true
            return false
        }
        return true
    }

    val title = remember { mutableStateOf(0) }
    val buttonText = remember { mutableStateOf(0) }

    when (pinCodeAction) {
        PinCodeAction.ENTER_PIN_CODE -> {
            title.value = R.string.enterPinCodeTitle
            buttonText.value = R.string.confirm
        }
        PinCodeAction.SETUP_PIN_CODE -> {
            title.value = R.string.setupPinTitle
            buttonText.value = when (pinCodeStatus.value) {
                PinCodeAction.CONFIRM_PIN_CODE -> R.string.confirm
                else -> R.string.next
            }
        }
        PinCodeAction.CHANGE_PIN_CODE -> {
            title.value = R.string.editPinCodeTitle
            buttonText.value = when (pinCodeStatus.value) {
                PinCodeAction.CONFIRM_PIN_CODE -> R.string.confirm
                else -> R.string.next
            }
        }
        else -> { title.value = R.string.shouldNotHappen }
    }

    val subtitleText = when (pinCodeAction) {
        PinCodeAction.SETUP_PIN_CODE -> when (pinCodeStatus.value) {
            PinCodeAction.CONFIRM_PIN_CODE -> "CONFIRM YOUR PIN"
            else -> "CHOOSE YOUR PIN"
        }
        PinCodeAction.CHANGE_PIN_CODE -> when (pinCodeStatus.value) {
            PinCodeAction.ENTER_PIN_CODE -> "ENTER CURRENT PIN"
            PinCodeAction.CHANGE_PIN_CODE -> "ENTER NEW PIN"
            PinCodeAction.CONFIRM_PIN_CODE -> "CONFIRM NEW PIN"
            else -> "ENTER YOUR PIN"
        }
        else -> "ENTER YOUR PIN"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HushColors.bg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderAlternateRow(
                onClick = { navController.popBackStack() },
                titleText = stringResource(title.value)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Lock icon
                Image(
                    painter = painterResource(id = R.drawable.ic_lock_outline),
                    contentDescription = "Lock",
                    modifier = Modifier.size(64.dp),
                    colorFilter = ColorFilter.tint(HushColors.textFaint)
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Subtitle
                Text(
                    text = subtitleText,
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        letterSpacing = 3.sp,
                        color = HushColors.textMuted
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))

                // PIN dots with glow
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val pinLen = activePinValue.value.length.coerceAtMost(16)
                    val dotsToShow = maxOf(4, pinLen)
                    for (i in 0 until dotsToShow) {
                        val isFilled = i < pinLen
                        Canvas(modifier = Modifier.size(if (isFilled) 18.dp else 10.dp)) {
                            val dotRadius = if (isFilled) 5.dp.toPx() else 5.dp.toPx()
                            if (isFilled) {
                                // Glow layer
                                drawCircle(
                                    color = HushColors.textMuted.copy(alpha = 0.15f),
                                    radius = dotRadius * 1.8f
                                )
                                // Solid dot
                                drawCircle(
                                    color = HushColors.textBright,
                                    radius = dotRadius
                                )
                            } else {
                                // Empty dot — outline only
                                drawCircle(
                                    color = HushColors.border,
                                    radius = dotRadius,
                                    style = Stroke(width = 1.dp.toPx())
                                )
                            }
                        }
                        if (i < dotsToShow - 1) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }

                // Warning box for PIN setup
                if (pinCodeAction == PinCodeAction.SETUP_PIN_CODE && pinCodeStatus.value == PinCodeAction.SETUP_PIN_CODE) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(12.dp),
                                ambientColor = HushColors.danger.copy(alpha = 0.15f),
                                spotColor = HushColors.danger.copy(alpha = 0.1f)
                            )
                            .background(HushColors.dangerBg, RoundedCornerShape(12.dp))
                            .border(1.dp, HushColors.dangerBorder, RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "If you forget your PIN, the card locks permanently. There is no recovery. Choose a PIN you will remember.",
                            style = TextStyle(
                                fontFamily = outfitFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = HushColors.danger,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }

                // Error
                if (showError.value) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(appError.value.msg),
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = HushColors.danger,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Number pad
                val numbers = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("", "0", "DEL")
                )

                numbers.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        row.forEach { key ->
                            if (key.isEmpty()) {
                                Spacer(modifier = Modifier.size(72.dp).padding(4.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .padding(4.dp)
                                        .shadow(
                                            elevation = 2.dp,
                                            shape = RoundedCornerShape(12.dp),
                                            ambientColor = Color.Black.copy(alpha = 0.3f),
                                            spotColor = Color.Black.copy(alpha = 0.2f)
                                        )
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color(0xFF161618),
                                                    Color(0xFF0E0E10),
                                                )
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.White.copy(alpha = 0.06f),
                                                    Color.White.copy(alpha = 0.02f),
                                                )
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .hushClickEffect(
                                            onClick = {
                                                showError.value = false
                                                if (key == "DEL") {
                                                    if (activePinValue.value.isNotEmpty()) {
                                                        activePinValue.value =
                                                            activePinValue.value.dropLast(1)
                                                    }
                                                } else {
                                                    if (activePinValue.value.length < 16) {
                                                        activePinValue.value += key
                                                    }
                                                }
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (key == "DEL") {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_backspace),
                                            contentDescription = "Delete",
                                            modifier = Modifier.size(24.dp),
                                            colorFilter = ColorFilter.tint(HushColors.textMuted)
                                        )
                                    } else {
                                        Text(
                                            text = key,
                                            style = TextStyle(
                                                fontFamily = outfitFamily,
                                                fontWeight = FontWeight.Light,
                                                fontSize = 28.sp,
                                                color = HushColors.textBody
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // CONFIRM / NEXT button
                val isEnabled = activePinValue.value.length >= 4
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .alpha(if (isEnabled) 1f else 0.4f)
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = Color.Black.copy(alpha = 0.4f),
                            spotColor = Color.Black.copy(alpha = 0.3f)
                        )
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF161618),
                                    Color(0xFF0E0E10),
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.06f),
                                    Color.White.copy(alpha = 0.02f),
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .hushClickEffect(
                            onClick = {
                                if (!isEnabled) return@hushClickEffect
                                when (pinCodeAction) {
                                    PinCodeAction.ENTER_PIN_CODE -> {
                                        if (!checkPinFormat(pin = curPinValue.value)) return@hushClickEffect
                                        viewModel.setPinStringForCard(pinString = curPinValue.value, isBackupCard = isBackupCard)
                                        viewModel.showNfcOverlayForScan()
                                        viewModel.scanCardForAction(
                                            activity = context as Activity,
                                            nfcActionType = if (isBackupCard) NfcActionType.SCAN_BACKUP_CARD else NfcActionType.SCAN_CARD
                                        )
                                    }
                                    PinCodeAction.SETUP_PIN_CODE -> {
                                        when (pinCodeStatus.value) {
                                            PinCodeAction.SETUP_PIN_CODE -> {
                                                if (!checkPinFormat(pin = curSetupPinValue.value)) return@hushClickEffect
                                                pinCodeStatus.value = PinCodeAction.CONFIRM_PIN_CODE
                                            }
                                            PinCodeAction.CONFIRM_PIN_CODE -> {
                                                if (!checkPinFormat(pin = curConfirmPinValue.value)) return@hushClickEffect
                                                if (curSetupPinValue.value != curConfirmPinValue.value) {
                                                    appError.value = AppErrorMsg.PIN_MISMATCH
                                                    showError.value = true
                                                    return@hushClickEffect
                                                }
                                                viewModel.setPinStringForCard(curSetupPinValue.value, isBackupCard = isBackupCard)
                                                viewModel.showNfcOverlayForScan()
                                                viewModel.scanCardForAction(
                                                    activity = context as Activity,
                                                    nfcActionType = if (isBackupCard) NfcActionType.SETUP_CARD_FOR_BACKUP else NfcActionType.SETUP_CARD
                                                )
                                            }
                                            else -> {}
                                        }
                                    }
                                    PinCodeAction.CHANGE_PIN_CODE -> {
                                        when (pinCodeStatus.value) {
                                            PinCodeAction.ENTER_PIN_CODE -> {
                                                if (!checkPinFormat(pin = curPinValue.value)) return@hushClickEffect
                                                pinCodeStatus.value = PinCodeAction.CHANGE_PIN_CODE
                                            }
                                            PinCodeAction.CHANGE_PIN_CODE -> {
                                                if (!checkPinFormat(pin = curChangePinValue.value)) return@hushClickEffect
                                                pinCodeStatus.value = PinCodeAction.CONFIRM_PIN_CODE
                                            }
                                            PinCodeAction.CONFIRM_PIN_CODE -> {
                                                if (!checkPinFormat(pin = curConfirmPinValue.value)) return@hushClickEffect
                                                if (curChangePinValue.value != curConfirmPinValue.value) {
                                                    appError.value = AppErrorMsg.PIN_MISMATCH
                                                    showError.value = true
                                                    return@hushClickEffect
                                                }
                                                viewModel.setPinStringForCard(curPinValue.value, isBackupCard = false)
                                                viewModel.changePinStringForCard(curChangePinValue.value)
                                                viewModel.showNfcOverlayForScan()
                                                viewModel.scanCardForAction(
                                                    activity = context as Activity,
                                                    nfcActionType = NfcActionType.CHANGE_PIN
                                                )
                                            }
                                            else -> {}
                                        }
                                    }
                                    else -> {}
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(buttonText.value).uppercase(),
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            letterSpacing = 3.sp,
                            color = HushColors.textMuted
                        )
                    )
                }

                // Card reminder for PIN entry
                if (pinCodeAction == PinCodeAction.ENTER_PIN_CODE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Keep your card held to the back of your phone",
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 11.sp,
                            color = HushColors.textGhost,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
