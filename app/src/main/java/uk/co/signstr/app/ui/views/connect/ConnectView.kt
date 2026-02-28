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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import uk.co.signstr.app.data.ApprovalPolicy
import uk.co.signstr.app.data.ApprovalPolicyStore
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
    var showQr by remember { mutableStateOf(connections.isEmpty()) }
    val bunkerUri = viewModel.bunkerUri.value
    var selectedConnection by remember { mutableStateOf<SignstrConnection?>(null) }

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
                        ConnectionCard(
                            connection = connection,
                            context = context,
                            onClick = { selectedConnection = connection },
                            onRemove = { viewModel.removeConnection(it) }
                        )
                    }
                }
            }
        }
    }

    // Connection detail dialog
    if (selectedConnection != null) {
        ConnectionDetailDialog(
            connection = selectedConnection!!,
            context = context,
            onDismiss = { selectedConnection = null },
            onRemove = {
                viewModel.removeConnection(selectedConnection!!)
                selectedConnection = null
            }
        )
    }
}

@Composable
private fun ConnectionCard(
    connection: SignstrConnection,
    context: Context,
    onClick: () -> Unit,
    onRemove: (SignstrConnection) -> Unit
) {
    val policy = ApprovalPolicyStore.getPolicy(context, connection.clientPubkeyHex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .signstrClickEffect(onClick = onClick)
            .background(SignstrColors.bgRaised, RoundedCornerShape(12.dp))
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
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = policy.label,
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 10.sp,
                        color = SignstrColors.textGhost
                    )
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
private fun ConnectionDetailDialog(
    connection: SignstrConnection,
    context: Context,
    onDismiss: () -> Unit,
    onRemove: () -> Unit
) {
    var policy by remember {
        mutableStateOf(ApprovalPolicyStore.getPolicy(context, connection.clientPubkeyHex))
    }
    var showPolicyPicker by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SignstrColors.bgRaised, RoundedCornerShape(16.dp))
                .border(1.dp, SignstrColors.border, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                Text(
                    text = "CONNECTION",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        letterSpacing = 4.sp,
                        color = SignstrColors.textFaint
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))

                DetailRow("CLIENT", connection.clientName.ifEmpty { "Unknown" })
                DetailRow("PUBKEY", connection.clientPubkeyHex.take(24) + "...")
                DetailRow("RELAYS", connection.relays.joinToString("\n"))
                DetailRow("POLICY", policy.label)

                Spacer(modifier = Modifier.height(16.dp))

                GhostButton(
                    text = "Change Approval Policy",
                    onClick = { showPolicyPicker = true }
                )
                Spacer(modifier = Modifier.height(8.dp))
                GhostButton(
                    text = "Remove Connection",
                    onClick = onRemove,
                    isDanger = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                GhostButton(
                    text = "Close",
                    onClick = onDismiss
                )
            }
        }
    }

    if (showPolicyPicker) {
        Dialog(onDismissRequest = { showPolicyPicker = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SignstrColors.bgRaised, RoundedCornerShape(16.dp))
                    .border(1.dp, SignstrColors.border, RoundedCornerShape(16.dp))
                    .padding(24.dp)
            ) {
                Column {
                    Text(
                        text = "APPROVAL POLICY",
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            letterSpacing = 4.sp,
                            color = SignstrColors.textFaint
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    for (p in ApprovalPolicy.entries) {
                        val isSelected = p == policy
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .signstrClickEffect(onClick = {
                                    policy = p
                                    ApprovalPolicyStore.setPolicy(context, connection.clientPubkeyHex, p)
                                    showPolicyPicker = false
                                })
                                .background(
                                    if (isSelected) SignstrColors.border else androidx.compose.ui.graphics.Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = p.label,
                                    style = TextStyle(
                                        fontFamily = outfitFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 13.sp,
                                        color = if (isSelected) SignstrColors.textBright else SignstrColors.textBody
                                    )
                                )
                                Text(
                                    text = p.description,
                                    style = TextStyle(
                                        fontFamily = outfitFamily,
                                        fontWeight = FontWeight.Light,
                                        fontSize = 10.sp,
                                        color = SignstrColors.textGhost
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                color = SignstrColors.textGhost
            )
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Light,
                fontSize = 13.sp,
                color = SignstrColors.textBody,
                lineHeight = 18.sp
            )
        )
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
