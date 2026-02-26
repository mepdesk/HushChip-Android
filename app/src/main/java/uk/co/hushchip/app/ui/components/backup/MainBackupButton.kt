package uk.co.hushchip.app.ui.components.backup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import uk.co.hushchip.app.R
import uk.co.hushchip.app.data.BackupStatus
import uk.co.hushchip.app.ui.components.shared.HushButton

@Composable
fun MainBackupButton(
    backupStatus: MutableState<BackupStatus>,
    onClick: () -> Unit
) {
    HushButton(
        onClick = {
            onClick()
        },
        text = when (backupStatus.value) {
            BackupStatus.DEFAULT -> {
                R.string.start
            }
            BackupStatus.FIRST_STEP -> {
                R.string.next
            }
            BackupStatus.SECOND_STEP -> {
                R.string.scanMySeedkeeper
            }
            BackupStatus.THIRD_STEP -> {
                R.string.makeBackup
            }
            BackupStatus.FOURTH_STEP -> {
                R.string.next
            }
            BackupStatus.SUCCESS -> {
                R.string.home
            }
            BackupStatus.FAILURE -> {
                R.string.home
            }
        }
    )
}