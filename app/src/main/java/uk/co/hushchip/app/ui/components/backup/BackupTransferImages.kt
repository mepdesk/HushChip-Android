package uk.co.hushchip.app.ui.components.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import uk.co.hushchip.app.R
import uk.co.hushchip.app.data.BackupStatus
import uk.co.hushchip.app.ui.components.shared.IllustrationPlaceholder

@Composable
fun BackupTransferImages(
    backupStatus: BackupStatus
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (backupStatus){
            BackupStatus.SUCCESS -> {
                IllustrationPlaceholder(
                    modifier = Modifier.size(200.dp),
                    label = "Backup complete"
                )
            }
            BackupStatus.FAILURE -> {}
            else -> {
                BackupTransferImage(
                    image = R.drawable.contactless_24px,
                    text = R.string.masterCard,
                    alpha = if (backupStatus == BackupStatus.FIRST_STEP || backupStatus == BackupStatus.FOURTH_STEP) 0.3f else 1f
                )
                IllustrationPlaceholder(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .width(60.dp)
                        .height(60.dp),
                    label = "→"
                )
                BackupTransferImage(
                    image = R.drawable.contactless_24px,
                    text = R.string.backupCard,
                    alpha = if (backupStatus == BackupStatus.SECOND_STEP) 0.3f else 1f
                )
            }
        }
    }
}
