package uk.co.signstr.app.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import uk.co.signstr.app.nip46.NIP46Service
import uk.co.signstr.app.ui.theme.SignstrColors
import uk.co.signstr.app.ui.theme.outfitFamily

@Composable
fun ApprovalDialog(
    request: NIP46Service.NIP46Request,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Dialog(onDismissRequest = onReject) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SignstrColors.bgRaised, RoundedCornerShape(16.dp))
                .border(1.dp, SignstrColors.border, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "SIGNING REQUEST",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        letterSpacing = 4.sp,
                        color = SignstrColors.textFaint
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Method
                Text(
                    text = request.method.replace("_", " "),
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 18.sp,
                        color = SignstrColors.textBright,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Client info
                Text(
                    text = "from ${request.connection.clientName.ifEmpty { request.clientPubkey.take(16) + "..." }}",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = SignstrColors.textMuted,
                        textAlign = TextAlign.Center
                    )
                )

                // Event kind if available
                request.eventKind?.let { kind ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .background(SignstrColors.bgSurface, RoundedCornerShape(6.dp))
                            .border(1.dp, SignstrColors.border, RoundedCornerShape(6.dp))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Kind $kind",
                            style = TextStyle(
                                fontFamily = outfitFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 12.sp,
                                color = SignstrColors.textBody
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GhostButton(
                        text = "Reject",
                        onClick = onReject,
                        isDanger = true,
                        modifier = Modifier.weight(1f)
                    )
                    GhostButton(
                        text = "Approve",
                        onClick = onApprove,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
