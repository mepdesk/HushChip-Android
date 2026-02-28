package uk.co.signstr.app.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.window.Dialog
import uk.co.signstr.app.ui.theme.SignstrColors
import uk.co.signstr.app.ui.theme.outfitFamily

@Composable
fun CreateIdentityDialog(
    onDismiss: () -> Unit,
    onCreateNew: (name: String) -> Unit,
    onImport: (name: String, nsec: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var nsec by remember { mutableStateOf("") }
    var isImport by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SignstrColors.bgRaised, RoundedCornerShape(16.dp))
                .border(1.dp, SignstrColors.border, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isImport) "IMPORT IDENTITY" else "NEW IDENTITY",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        letterSpacing = 4.sp,
                        color = SignstrColors.textFaint
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Name field
                Text(
                    text = "NAME",
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 9.sp,
                        letterSpacing = 2.sp,
                        color = SignstrColors.textGhost
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    textStyle = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp,
                        color = SignstrColors.textBright
                    ),
                    cursorBrush = SolidColor(SignstrColors.textMuted),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SignstrColors.bgSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, SignstrColors.border, RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    decorationBox = { innerTextField ->
                        Box {
                            if (name.isEmpty()) {
                                Text(
                                    text = "e.g. Main, Work, Alt...",
                                    style = TextStyle(
                                        fontFamily = outfitFamily,
                                        fontWeight = FontWeight.Light,
                                        fontSize = 14.sp,
                                        color = SignstrColors.textGhost
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                if (isImport) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "NSEC",
                        modifier = Modifier.fillMaxWidth(),
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 9.sp,
                            letterSpacing = 2.sp,
                            color = SignstrColors.textGhost
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
                            .padding(12.dp),
                        decorationBox = { innerTextField ->
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
                                innerTextField()
                            }
                        }
                    )
                }

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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GhostButton(
                        text = if (isImport) "Generate" else "Import",
                        onClick = { isImport = !isImport; error = "" },
                        modifier = Modifier.weight(1f)
                    )
                    GhostButton(
                        text = if (isImport) "Import" else "Create",
                        onClick = {
                            val trimmedName = name.trim().ifEmpty { "Identity" }
                            if (isImport) {
                                val trimmedNsec = nsec.trim()
                                if (!trimmedNsec.startsWith("nsec1")) {
                                    error = "Must start with nsec1"
                                    return@GhostButton
                                }
                                onImport(trimmedName, trimmedNsec)
                            } else {
                                onCreateNew(trimmedName)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = if (isImport) nsec.isNotBlank() else true
                    )
                }
            }
        }
    }
}
