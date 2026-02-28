package uk.co.signstr.app.ui.views.identity

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.signstr.app.crypto.Bech32
import uk.co.signstr.app.crypto.NIP44
import uk.co.signstr.app.ui.components.shared.*
import uk.co.signstr.app.ui.theme.SignstrColors
import uk.co.signstr.app.ui.theme.outfitFamily
import uk.co.signstr.app.utils.ClipboardUtil
import uk.co.signstr.app.utils.signstrClickEffect
import uk.co.signstr.app.viewmodels.SignstrViewModel

@Composable
fun IdentityView(
    context: Context,
    viewModel: SignstrViewModel,
    onCreateIdentity: () -> Unit
) {
    val activeIdentity = viewModel.activeIdentity.value
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SignstrColors.bg)
            .verticalScroll(scrollState)
    ) {
        IdentityPicker(
            identities = viewModel.identities,
            activeIdentity = activeIdentity,
            onSelect = { viewModel.setActiveIdentity(it) }
        )

        Column(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (activeIdentity == null) {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = "No identity yet",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        color = SignstrColors.textFaint,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                GhostButton(text = "Create Identity", onClick = onCreateIdentity)
                return
            }

            // Identity name
            Text(
                text = activeIdentity.name,
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 20.sp,
                    color = SignstrColors.textBright,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // npub QR
            val pubkeyBytes = NIP44.hexToBytes(activeIdentity.pubkeyHex)
            val npub = Bech32.bytesToNpub(pubkeyBytes)
            QrCodeDisplay(data = npub)
            Spacer(modifier = Modifier.height(12.dp))

            // npub text
            SectionLabel("NPUB")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SignstrColors.bgSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, SignstrColors.border, RoundedCornerShape(8.dp))
                    .signstrClickEffect(onClick = {
                        ClipboardUtil.copyWithAutoClear(context, "npub", npub)
                    })
                    .padding(12.dp)
            ) {
                Text(
                    text = npub,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = SignstrColors.textBody,
                        lineHeight = 16.sp
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Tap to copy",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 10.sp,
                    color = SignstrColors.textGhost
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Hex pubkey
            SectionLabel("HEX PUBKEY")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SignstrColors.bgSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, SignstrColors.border, RoundedCornerShape(8.dp))
                    .signstrClickEffect(onClick = {
                        ClipboardUtil.copyWithAutoClear(context, "Pubkey", activeIdentity.pubkeyHex)
                    })
                    .padding(12.dp)
            ) {
                Text(
                    text = activeIdentity.pubkeyHex,
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = SignstrColors.textFaint,
                        lineHeight = 16.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Add new identity button
            GhostButton(text = "Add Identity", onClick = onCreateIdentity)

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                color = SignstrColors.textFaint
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        HorizontalDivider(
            color = SignstrColors.border.copy(alpha = 0.5f),
            modifier = Modifier.weight(1f),
            thickness = 0.5.dp
        )
    }
}
