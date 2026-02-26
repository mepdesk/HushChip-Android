package uk.co.hushchip.app.ui.views.addsecret

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import uk.co.hushchip.app.ImportSecretView
import uk.co.hushchip.app.R
import uk.co.hushchip.app.data.ImportMode
import uk.co.hushchip.app.ui.components.home.SecretTypeIcon
import uk.co.hushchip.app.ui.components.shared.HeaderAlternateRow
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.ui.theme.outfitFamily
import uk.co.hushchip.app.utils.hushClickEffect
import uk.co.hushchip.app.viewmodels.SharedViewModel
import org.satochip.client.seedkeeper.SeedkeeperSecretType

@Composable
fun AddSecretView(
    context: Context,
    viewModel: SharedViewModel,
    navController: NavHostController,
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HushColors.bg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HeaderAlternateRow(
                titleText = "New Secret",
                onClick = { navController.popBackStack() }
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(12.dp))

                // Section header
                Text(
                    text = "ADD SECRET",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        letterSpacing = 3.sp,
                        color = HushColors.textBody
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Mnemonic
                SecretTypeCard(
                    iconType = SeedkeeperSecretType.BIP39_MNEMONIC,
                    title = "Mnemonic",
                    description = "BIP39 seed phrase (12, 18, or 24 words)",
                    onClick = {
                        navController.navigate(
                            ImportSecretView(importMode = ImportMode.IMPORT_A_SECRET.name)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Password
                SecretTypeCard(
                    iconType = SeedkeeperSecretType.PASSWORD,
                    title = "Password",
                    description = "Login credentials with URL",
                    onClick = {
                        navController.navigate(
                            ImportSecretView(importMode = ImportMode.IMPORT_A_SECRET.name)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Wallet Descriptor (SOON)
                SecretTypeCard(
                    iconType = SeedkeeperSecretType.WALLET_DESCRIPTOR,
                    title = "Wallet Descriptor",
                    description = "Bitcoin output descriptor",
                    isDisabled = true,
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Free Text
                SecretTypeCard(
                    iconType = SeedkeeperSecretType.DATA,
                    title = "Free Text",
                    description = "Any text data you want to store",
                    onClick = {
                        navController.navigate(
                            ImportSecretView(importMode = ImportMode.IMPORT_A_SECRET.name)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Master Seed (SOON)
                SecretTypeCard(
                    iconType = SeedkeeperSecretType.PUBKEY,
                    title = "Master Seed",
                    description = "Raw master seed bytes",
                    isDisabled = true,
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Generate section
                Text(
                    text = "GENERATE",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        letterSpacing = 3.sp,
                        color = HushColors.textBody
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                SecretTypeCard(
                    iconType = SeedkeeperSecretType.BIP39_MNEMONIC,
                    title = "Generate Mnemonic",
                    description = "Create a new random seed phrase",
                    onClick = {
                        navController.navigate(
                            ImportSecretView(importMode = ImportMode.GENERATE_A_SECRET.name)
                        )
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))

                SecretTypeCard(
                    iconType = SeedkeeperSecretType.PASSWORD,
                    title = "Generate Password",
                    description = "Create a new random password",
                    onClick = {
                        navController.navigate(
                            ImportSecretView(importMode = ImportMode.GENERATE_A_SECRET.name)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SecretTypeCard(
    iconType: SeedkeeperSecretType,
    title: String,
    description: String,
    isDisabled: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (isDisabled) 0.5f else 1f)
            .then(
                if (!isDisabled) {
                    Modifier.hushClickEffect(onClick = onClick)
                } else {
                    Modifier
                }
            )
            .background(HushColors.bgRaised, RoundedCornerShape(12.dp))
            .border(1.dp, HushColors.border, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SecretTypeIcon(type = iconType)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = HushColors.textBody
                )
            )
            Text(
                text = description,
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp,
                    color = HushColors.textMuted
                )
            )
        }
        if (isDisabled) {
            Text(
                text = "SOON",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    color = HushColors.textFaint
                )
            )
        } else {
            Text(
                text = "\u203A",
                style = TextStyle(
                    fontSize = 20.sp,
                    color = HushColors.textFaint
                )
            )
        }
    }
}
