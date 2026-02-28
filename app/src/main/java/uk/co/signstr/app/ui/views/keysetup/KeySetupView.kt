package uk.co.signstr.app.ui.views.keysetup

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.signstr.app.data.SignstrIdentity
import uk.co.signstr.app.ui.components.shared.GhostButton
import uk.co.signstr.app.ui.theme.SignstrColors
import uk.co.signstr.app.ui.theme.outfitFamily
import uk.co.signstr.app.viewmodels.SignstrViewModel

@Composable
fun KeySetupView(
    onIdentityCreated: (SignstrIdentity) -> Unit,
    viewModel: SignstrViewModel,
    context: Context
) {
    var showImport by remember { mutableStateOf(false) }
    var nsec by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SignstrColors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.2f))

        Text(
            text = "SIGNSTR",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                letterSpacing = 5.sp,
                color = SignstrColors.textMuted
            )
        )
        Spacer(modifier = Modifier.height(40.dp))

        if (!showImport) {
            Text(
                text = "Set up your identity",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 16.sp,
                    letterSpacing = 0.5.sp,
                    color = SignstrColors.textBright,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Create a new Nostr keypair or import\nan existing nsec to get started.",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp,
                    color = SignstrColors.textMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            )
            Spacer(modifier = Modifier.height(40.dp))

            // Create card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SignstrColors.bgRaised, RoundedCornerShape(12.dp))
                    .border(1.dp, SignstrColors.border, RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CREATE NEW IDENTITY",
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            letterSpacing = 4.sp,
                            color = SignstrColors.textBright,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Generate a fresh secp256k1 keypair",
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 12.sp,
                            color = SignstrColors.textFaint,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GhostButton(
                        text = "Generate Keypair",
                        onClick = {
                            val identity = viewModel.createIdentity("Main")
                            onIdentityCreated(identity)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Import card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SignstrColors.bgRaised, RoundedCornerShape(12.dp))
                    .border(1.dp, SignstrColors.border, RoundedCornerShape(12.dp))
                    .padding(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "IMPORT EXISTING NSEC",
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            letterSpacing = 4.sp,
                            color = SignstrColors.textBright,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Paste your nsec1... private key",
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 12.sp,
                            color = SignstrColors.textFaint,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    GhostButton(
                        text = "Import Key",
                        onClick = { showImport = true }
                    )
                }
            }
        } else {
            // Import mode
            Text(
                text = "IMPORT EXISTING NSEC",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    letterSpacing = 4.sp,
                    color = SignstrColors.textFaint
                )
            )
            Spacer(modifier = Modifier.height(24.dp))

            BasicTextField(
                value = nsec,
                onValueChange = { nsec = it; error = "" },
                textStyle = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 13.sp,
                    color = SignstrColors.textBright
                ),
                cursorBrush = SolidColor(SignstrColors.textMuted),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SignstrColors.bgSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, SignstrColors.border, RoundedCornerShape(8.dp))
                    .padding(14.dp),
                decorationBox = { inner ->
                    Box {
                        if (nsec.isEmpty()) {
                            Text(
                                text = "nsec1...",
                                style = TextStyle(
                                    fontFamily = outfitFamily,
                                    fontWeight = FontWeight.Light,
                                    fontSize = 13.sp,
                                    color = SignstrColors.textGhost
                                )
                            )
                        }
                        inner()
                    }
                }
            )

            if (error.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 11.sp,
                        color = SignstrColors.danger
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            GhostButton(
                text = "Import",
                onClick = {
                    val trimmed = nsec.trim()
                    if (!trimmed.startsWith("nsec1")) {
                        error = "Must start with nsec1"
                        return@GhostButton
                    }
                    val identity = viewModel.importIdentity("Main", trimmed)
                    if (identity != null) {
                        onIdentityCreated(identity)
                    } else {
                        error = "Invalid nsec or already imported"
                    }
                },
                enabled = nsec.isNotBlank()
            )
            Spacer(modifier = Modifier.height(12.dp))
            GhostButton(
                text = "Back",
                onClick = { showImport = false; error = "" },
                isDanger = false
            )
        }

        Spacer(modifier = Modifier.weight(0.3f))
    }
}
