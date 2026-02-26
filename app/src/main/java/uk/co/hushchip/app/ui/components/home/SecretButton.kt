package uk.co.hushchip.app.ui.components.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.satochip.client.seedkeeper.SeedkeeperSecretHeader
import org.satochip.client.seedkeeper.SeedkeeperSecretType
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.ui.theme.outfitFamily
import uk.co.hushchip.app.utils.hushClickEffect

@Composable
fun SecretButton(
    modifier: Modifier = Modifier,
    secretHeader: SeedkeeperSecretHeader? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .hushClickEffect(onClick = { onClick() })
            .background(
                color = HushColors.bgRaised,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = HushColors.border,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        secretHeader?.let {
            SecretTypeIcon(type = secretHeader.type)
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = secretHeader.label,
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 15.sp,
                        color = HushColors.textBody
                    ),
                    maxLines = 1
                )
                Text(
                    text = getTypeName(secretHeader.type),
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = HushColors.textMuted
                    )
                )
            }

            Text(
                text = "\u203A",
                style = TextStyle(
                    fontSize = 20.sp,
                    color = HushColors.textFaint
                )
            )
        } ?: run {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "+",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 24.sp,
                    color = HushColors.textBody
                )
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SecretTypeIcon(type: SeedkeeperSecretType) {
    val iconText = when (type) {
        SeedkeeperSecretType.PASSWORD -> "\u25CF"
        SeedkeeperSecretType.MASTERSEED, SeedkeeperSecretType.BIP39_MNEMONIC, SeedkeeperSecretType.ELECTRUM_MNEMONIC -> "Aa"
        SeedkeeperSecretType.WALLET_DESCRIPTOR -> "{ }"
        SeedkeeperSecretType.DATA -> "T"
        SeedkeeperSecretType.PUBKEY -> "S"
        else -> "?"
    }

    Box(
        modifier = Modifier
            .size(28.dp)
            .background(
                color = HushColors.bgSurface,
                shape = RoundedCornerShape(6.dp)
            )
            .border(
                width = 1.dp,
                color = HushColors.border,
                shape = RoundedCornerShape(6.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = iconText,
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = if (iconText.length > 2) 8.sp else 10.sp,
                color = HushColors.textFaint,
                textAlign = TextAlign.Center
            )
        )
    }
}

private fun getTypeName(type: SeedkeeperSecretType): String {
    return when (type) {
        SeedkeeperSecretType.PASSWORD -> "Password"
        SeedkeeperSecretType.MASTERSEED, SeedkeeperSecretType.BIP39_MNEMONIC -> "Mnemonic"
        SeedkeeperSecretType.ELECTRUM_MNEMONIC -> "Electrum Mnemonic"
        SeedkeeperSecretType.WALLET_DESCRIPTOR -> "Wallet Descriptor"
        SeedkeeperSecretType.DATA -> "Free Text"
        SeedkeeperSecretType.PUBKEY -> "Public Key"
        else -> "Secret"
    }
}
