package uk.co.signstr.app.ui.views.keysetup

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.signstr.app.ui.components.shared.GhostButton
import uk.co.signstr.app.ui.theme.SignstrColors
import uk.co.signstr.app.ui.theme.outfitFamily
import uk.co.signstr.app.utils.ClipboardUtil

@Composable
fun BackupKeyView(
    nsec: String,
    context: Context,
    onComplete: () -> Unit
) {
    var revealed by remember { mutableStateOf(false) }
    var showSkipWarning by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SignstrColors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "BACK UP YOUR KEY",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                letterSpacing = 4.sp,
                color = SignstrColors.textFaint
            )
        )
        Spacer(modifier = Modifier.height(24.dp))

        // Warning box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SignstrColors.dangerBg, RoundedCornerShape(8.dp))
                .border(1.dp, SignstrColors.dangerBorder, RoundedCornerShape(8.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "Your Nostr identity depends on this key. If you lose this device and have no backup, your identity is gone forever. There is no recovery. No password reset. No support ticket.",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp,
                    color = SignstrColors.danger,
                    lineHeight = 20.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Nsec display
        Text(
            text = "YOUR NSEC",
            modifier = Modifier.fillMaxWidth(),
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                color = SignstrColors.textGhost
            )
        )
        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SignstrColors.bgSurface, RoundedCornerShape(8.dp))
                .border(1.dp, SignstrColors.border, RoundedCornerShape(8.dp))
                .padding(14.dp)
        ) {
            Text(
                text = if (revealed) nsec else "nsec1" + "\u2022".repeat(40),
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    color = if (revealed) SignstrColors.textBody else SignstrColors.textGhost,
                    lineHeight = 16.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GhostButton(
                text = if (revealed) "Hide" else "Reveal",
                onClick = { revealed = !revealed },
                modifier = Modifier.weight(1f),
                isDanger = true
            )
            GhostButton(
                text = "Copy",
                onClick = { ClipboardUtil.copyWithAutoClear(context, "nsec", nsec) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        GhostButton(
            text = "I've saved it somewhere safe",
            onClick = onComplete
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!showSkipWarning) {
            GhostButton(
                text = "I'll do this later",
                onClick = { showSkipWarning = true },
                isDanger = true
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SignstrColors.dangerBg, RoundedCornerShape(8.dp))
                    .border(1.dp, SignstrColors.dangerBorder, RoundedCornerShape(8.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "If you lose this device without a backup, your identity is gone forever.",
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 12.sp,
                            color = SignstrColors.danger,
                            lineHeight = 18.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    GhostButton(
                        text = "I understand the risk",
                        onClick = onComplete,
                        isDanger = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
