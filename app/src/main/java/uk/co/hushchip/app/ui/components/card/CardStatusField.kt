package uk.co.hushchip.app.ui.components.card

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import uk.co.hushchip.app.ui.theme.HushColors
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.satochip.client.seedkeeper.SeedkeeperStatus
import uk.co.hushchip.app.R

@Composable
fun CardStatusField(
    modifier: Modifier = Modifier,
    title: Int,
    cardAppletVersion: String,
    cardAuthentikey: String,
    seedkeeperStatus: SeedkeeperStatus? = null
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = title),
            style = TextStyle(
                color = HushColors.textBright,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .padding(bottom = 32.dp, top = 16.dp)
                .border(
                    color = HushColors.border,
                    width = 2.dp,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = cardAppletVersion,
                    style = TextStyle(
                        color = HushColors.textBody,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic
                    )
                )
                seedkeeperStatus?.let { status ->
                    Text(
                        text = stringResource(id = R.string.secretsStored) + ": ${status.nbSecrets}",
                        style = TextStyle(
                            color = HushColors.textBody,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic
                        )
                    )
                    Text(
                        text = stringResource(id = R.string.memoryAvailable) + ": ${status.freeMemory} bytes",
                        style = TextStyle(
                            color = HushColors.textBody,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic
                        )
                    )
                    Text(
                        text = stringResource(id = R.string.memoryTotal) + ": ${status.totalMemory} bytes",
                        style = TextStyle(
                            color = HushColors.textBody,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(id = R.string.cardAuthentikey) + ":",
                    style = TextStyle(
                        color = HushColors.textBody,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic
                    )
                )
                Text(
                    text = cardAuthentikey,
                    style = TextStyle(
                        color = HushColors.textBody,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}