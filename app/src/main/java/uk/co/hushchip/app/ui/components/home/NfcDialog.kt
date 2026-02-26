package uk.co.hushchip.app.ui.components.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import kotlinx.coroutines.delay
import uk.co.hushchip.app.R
import uk.co.hushchip.app.data.NfcResultCode
import uk.co.hushchip.app.ui.components.shared.BottomDrawer
import kotlin.time.Duration.Companion.seconds

private const val TAG = "NfcDialog"

@Composable
fun NfcDialog(
    openDialogCustom: MutableState<Boolean>,
    resultCodeLive: NfcResultCode,
    isConnected: Boolean,
    progress: Float? = null,
) {
    BottomDrawer(
        showSheet = openDialogCustom
    ) {
        if (resultCodeLive == NfcResultCode.BUSY) {
            // Busy scanning
            if (isConnected) {
                DrawerScreen(
                    closeSheet = {
                        openDialogCustom.value = !openDialogCustom.value
                    },
                    message = R.string.scanning,
                    image = R.drawable.nfc_scanner,
                    progress = progress,
                )
            } else {
                DrawerScreen(
                    closeSheet = {
                        openDialogCustom.value = !openDialogCustom.value
                    },
                    closeDrawerButton = true,
                    title = R.string.readyToScan,
                    image = R.drawable.phone_icon,
                    message = R.string.nfcHoldSeedkeeper
                )
            }
        } else {
            // finished scanning with some result code
            DrawerScreen(
                closeSheet = {
                    openDialogCustom.value = !openDialogCustom.value
                },
                title = resultCodeLive.resTitle,
                image = resultCodeLive.resImage,
                message = resultCodeLive.resMsg,
                colorFilter = if (resultCodeLive.resTitle == R.string.nfcTitleWarning) ColorFilter.tint(
                    Color.Red
                ) else null,
                triesLeft = resultCodeLive.triesLeft
            )
            LaunchedEffect(Unit) {
                // automatically close nfc toast after some delay
                delay(2.seconds)
                openDialogCustom.value = false
            }
        }
    }
}