package uk.co.hushchip.app.ui.components.backup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Color
import uk.co.hushchip.app.R
import uk.co.hushchip.app.data.BackupStatus
import uk.co.hushchip.app.ui.components.shared.SatoButton

@Composable
fun SecondaryBackupButton(
    backupStatus: MutableState<BackupStatus>,
    goBack: () -> Unit,
) {
    SatoButton(
        onClick = {
            goBack()
        },
        buttonColor = Color.Transparent,
        textColor = Color.Black,
        text = when (backupStatus.value) {
            BackupStatus.FIRST_STEP -> {
                R.string.back
            }
            else -> {
                R.string.restart
            }
        }
    )
}