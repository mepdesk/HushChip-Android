package uk.co.signstr.app.ui.views.connect

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.signstr.app.data.SignstrConnection
import uk.co.signstr.app.ui.components.shared.*
import uk.co.signstr.app.ui.theme.SignstrColors
import uk.co.signstr.app.ui.theme.outfitFamily
import uk.co.signstr.app.utils.ClipboardUtil
import uk.co.signstr.app.utils.signstrClickEffect
import uk.co.signstr.app.viewmodels.SignstrViewModel

@Composable
fun ConnectView(
    context: Context,
    viewModel: SignstrViewModel
) {
    val activeIdentity = viewModel.activeIdentity.value
    val connections = viewModel.getActiveConnections()
    // Auto-show QR when there are no connections yet
    var showQr by remember { mutableStateOf(connections.isEmpty()) }
    val bunkerUri = viewModel.bunkerUri.value

    LaunchedEffect(activeIdentity) {
        viewModel.updateBunkerUri()
        if (connections.isEmpty()) showQr = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SignstrColors.bg)
    ) {
        IdentityPicker(
            identities = viewModel.identities,
            activeIdentity = activeIdentity,
            onSelect = { viewModel.setActiveIdentity(it) }
        )

        if (activeIdentity == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Create an identity to get started",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp,
                        color = SignstrColors.textFaint
                    )
                )
            }
            return
        }

        if (showQr && bunkerUri.isNotEmpty()) {
            // QR Code display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SectionLabel("BUNKER QR")
                Spacer(modifier = Modifier.height(16.dp))
                QrCodeDisplay(data = bunkerUri)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Scan with your Nostr client",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = SignstrColors.textFaint,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    GhostButton(
                        text = "Copy URI",
                        onClick = { ClipboardUtil.copyWithAutoClear(context, "Bunker URI", bunkerUri) },
                        modifier = Modifier.weight(1f)
                    )
                    GhostButton(
                        text = "Close",
                        onClick = { showQr = false },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        } else {
            // Connections list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                GhostButton(
                    text = "Show Bunker QR",
                    onClick = {
                        viewModel.updateBunkerUri()
                        showQr = true
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))
                SectionLabel("CONNECTIONS")
            }

            if (connections.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No connections yet.\nShow your bunker QR or scan\na client's nostrconnect:// code.",
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 13.sp,
                            color = SignstrColors.textFaint,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(connections) { connection ->
                        ConnectionCard(connection, onRemove = { viewModel.removeConnection(it) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionCard(connection: SignstrConnection, onRemove: (SignstrConnection) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(SignstrColors.bgRaised, Color(0xFF0C0C0E))
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .border(1.dp, SignstrColors.border, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = connection.clientName.ifEmpty { "Unknown client" },
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = SignstrColors.textBody
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = connection.clientPubkeyHex.take(16) + "...",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 11.sp,
                        color = SignstrColors.textFaint
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "REMOVE",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 9.sp,
                    letterSpacing = 2.sp,
                    color = SignstrColors.danger
                ),
                modifier = Modifier.signstrClickEffect(onClick = { onRemove(connection) })
            )
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
