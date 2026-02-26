package uk.co.hushchip.app.ui.components.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import uk.co.hushchip.app.ui.theme.HushColors
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.satochip.client.seedkeeper.SeedkeeperSecretType
import uk.co.hushchip.app.R
import uk.co.hushchip.app.utils.hushClickEffect

@Composable
fun SecretsFilter(
    modifier: Modifier = Modifier,
    onClick: (SeedkeeperSecretType) -> Unit,
) {
    var isExpanded by remember {
        mutableStateOf(false)
    }
    var selectedLogType by remember {
        mutableStateOf(SeedkeeperSecretType.DEFAULT_TYPE)
    }
    val seedkeeperSecretTypeMap = hashMapOf(
        SeedkeeperSecretType.DEFAULT_TYPE to stringResource(id = R.string.all),
        SeedkeeperSecretType.BIP39_MNEMONIC to stringResource(id = R.string.mnemonic),
        SeedkeeperSecretType.PASSWORD to stringResource(id = R.string.password),
        SeedkeeperSecretType.DATA to stringResource(id = R.string.data),
        SeedkeeperSecretType.WALLET_DESCRIPTOR to stringResource(id = R.string.descriptors)
    )

    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
//            Text(
//                modifier = Modifier.clickable {
//                    isExpanded = true
//                },
//                textAlign = TextAlign.Start,
//                fontSize = 16.sp,
//                fontWeight = FontWeight.Medium,
//                color = Color.Black,
//                text = seedkeeperSecretTypeMap.getValue(selectedLogType)
//            )
            Image(
                painter = painterResource(R.drawable.filter),
                contentDescription = null,
                modifier = Modifier
                    .size(24.dp)
                    .hushClickEffect(
                        onClick = {
                            isExpanded = !isExpanded
                        }
                    ),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(HushColors.textMuted)
            )
        }

        DropdownMenu(
            modifier = Modifier
                .background(
                    color = HushColors.bgRaised,
                ),
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            DropdownMenuItem(
                modifier = Modifier
                    .background(
                        color = if (selectedLogType == SeedkeeperSecretType.DEFAULT_TYPE) HushColors.textFaint.copy(
                            alpha = 0.2f
                        ) else HushColors.bgRaised,
                    ),
                onClick = {
                    selectedLogType = SeedkeeperSecretType.DEFAULT_TYPE
                    isExpanded = false
                    onClick(selectedLogType)
                },
                text = {
                    Text(
                        textAlign = TextAlign.Start,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = HushColors.textBody,
                        text = seedkeeperSecretTypeMap.getValue(SeedkeeperSecretType.DEFAULT_TYPE)
                    )
                }
            )
            DropdownMenuItem(
                modifier = Modifier
                    .background(
                        color = if (selectedLogType == SeedkeeperSecretType.BIP39_MNEMONIC) HushColors.textFaint.copy(
                            alpha = 0.2f
                        ) else HushColors.bgRaised,
                    ),
                onClick = {
                    selectedLogType = SeedkeeperSecretType.BIP39_MNEMONIC
                    isExpanded = false
                    onClick(selectedLogType)
                },
                text = {
                    Text(
                        textAlign = TextAlign.Start,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = HushColors.textBody,
                        text = seedkeeperSecretTypeMap.getValue(SeedkeeperSecretType.BIP39_MNEMONIC)
                    )
                }
            )
            DropdownMenuItem(
                modifier = Modifier
                    .background(
                        color = if (selectedLogType == SeedkeeperSecretType.PASSWORD) HushColors.textFaint.copy(
                            alpha = 0.2f
                        ) else HushColors.bgRaised,
                    ),
                onClick = {
                    selectedLogType = SeedkeeperSecretType.PASSWORD
                    isExpanded = false
                    onClick(selectedLogType)
                },
                text = {
                    Text(
                        textAlign = TextAlign.Start,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = HushColors.textBody,
                        text = seedkeeperSecretTypeMap.getValue(SeedkeeperSecretType.PASSWORD)
                    )
                }
            )
            DropdownMenuItem(
                modifier = Modifier
                    .background(
                        color = if (selectedLogType == SeedkeeperSecretType.WALLET_DESCRIPTOR) HushColors.textFaint.copy(
                            alpha = 0.2f
                        ) else HushColors.bgRaised,
                    ),
                onClick = {
                    selectedLogType = SeedkeeperSecretType.WALLET_DESCRIPTOR
                    isExpanded = false
                    onClick(selectedLogType)
                },
                text = {
                    Text(
                        textAlign = TextAlign.Start,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = HushColors.textBody,
                        text = seedkeeperSecretTypeMap.getValue(SeedkeeperSecretType.WALLET_DESCRIPTOR)
                    )
                }
            )
            DropdownMenuItem(
                modifier = Modifier
                    .background(
                        color = if (selectedLogType == SeedkeeperSecretType.DATA) HushColors.textFaint.copy(
                            alpha = 0.2f
                        ) else HushColors.bgRaised,
                    ),
                onClick = {
                    selectedLogType = SeedkeeperSecretType.DATA
                    isExpanded = false
                    onClick(selectedLogType)
                },
                text = {
                    Text(
                        textAlign = TextAlign.Start,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = HushColors.textBody,
                        text = seedkeeperSecretTypeMap.getValue(SeedkeeperSecretType.DATA)
                    )
                }
            )
        }
    }
}