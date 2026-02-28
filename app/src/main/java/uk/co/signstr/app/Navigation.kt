package uk.co.signstr.app

import android.app.Activity
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.signstr.app.ui.components.shared.*
import uk.co.signstr.app.ui.theme.SignstrColors
import uk.co.signstr.app.ui.theme.outfitFamily
import uk.co.signstr.app.ui.views.connect.ConnectView
import uk.co.signstr.app.ui.views.events.EventsView
import uk.co.signstr.app.ui.views.identity.IdentityView
import uk.co.signstr.app.ui.views.settings.SettingsView
import uk.co.signstr.app.ui.views.splash.SplashView
import uk.co.signstr.app.utils.signstrClickEffect
import uk.co.signstr.app.viewmodels.SignstrViewModel
import kotlinx.coroutines.delay

enum class SignstrTab(val label: String) {
    CONNECT("CONNECT"),
    EVENTS("EVENTS"),
    IDENTITY("IDENTITY"),
    SETTINGS("SETTINGS")
}

@Composable
fun SignstrNavigation(
    context: Context,
    viewModel: SignstrViewModel
) {
    var showSplash by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf(SignstrTab.CONNECT) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    // Splash timer
    LaunchedEffect(Unit) {
        viewModel.initialize()
        delay(1500)
        showSplash = false
        // If no identities, show create dialog
        if (viewModel.identities.isEmpty()) {
            showCreateDialog = true
        }
    }

    // Approval dialog
    val showApproval = viewModel.showApprovalDialog.value
    val pendingRequest = viewModel.pendingRequest.value

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(visible = showSplash, enter = fadeIn(), exit = fadeOut()) {
            SplashView()
        }

        AnimatedVisibility(visible = !showSplash, enter = fadeIn(), exit = fadeOut()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Content area
                Box(modifier = Modifier.weight(1f)) {
                    when (currentTab) {
                        SignstrTab.CONNECT -> ConnectView(context, viewModel)
                        SignstrTab.EVENTS -> EventsView(viewModel)
                        SignstrTab.IDENTITY -> IdentityView(context, viewModel, onCreateIdentity = { showCreateDialog = true })
                        SignstrTab.SETTINGS -> SettingsView(context, viewModel, onExportNsec = { showExportDialog = true })
                    }
                }

                // Tab bar
                TabBar(currentTab = currentTab, onTabSelected = { currentTab = it })
            }
        }

        // Create identity dialog
        if (showCreateDialog) {
            CreateIdentityDialog(
                onDismiss = { showCreateDialog = false },
                onCreateNew = { name ->
                    viewModel.createIdentity(name)
                    showCreateDialog = false
                    Toast.makeText(context, "Identity created", Toast.LENGTH_SHORT).show()
                },
                onImport = { name, nsec ->
                    val identity = viewModel.importIdentity(name, nsec)
                    if (identity != null) {
                        showCreateDialog = false
                        Toast.makeText(context, "Identity imported", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Invalid nsec", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // Export nsec dialog
        if (showExportDialog) {
            val identity = viewModel.activeIdentity.value
            if (identity != null) {
                val nsec = remember { viewModel.getNsec(identity.id) }
                if (nsec != null) {
                    ExportNsecDialog(
                        nsec = nsec,
                        context = context,
                        onDismiss = { showExportDialog = false }
                    )
                }
            }
        }

        // Approval dialog
        if (showApproval && pendingRequest != null) {
            ApprovalDialog(
                request = pendingRequest,
                onApprove = {
                    // Biometric prompt would go here for non-safe kinds
                    viewModel.approveRequest()
                },
                onReject = { viewModel.rejectRequest() }
            )
        }
    }
}

@Composable
private fun TabBar(currentTab: SignstrTab, onTabSelected: (SignstrTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(SignstrColors.bg)
            .padding(top = 1.dp)
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        for (tab in SignstrTab.entries) {
            val isActive = tab == currentTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .signstrClickEffect(onClick = { onTabSelected(tab) })
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = tab.label,
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 9.sp,
                        letterSpacing = 2.sp,
                        color = if (isActive) SignstrColors.textMuted else SignstrColors.textGhost
                    )
                )
            }
        }
    }
}

@Composable
private fun ExportNsecDialog(nsec: String, context: Context, onDismiss: () -> Unit) {
    var revealed by remember { mutableStateOf(false) }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SignstrColors.bgRaised, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "EXPORT NSEC",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        letterSpacing = 4.sp,
                        color = SignstrColors.danger
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Warning
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SignstrColors.dangerBg, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Anyone who sees this key controls your Nostr identity. Only export if you need to recover or migrate.",
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 12.sp,
                            color = SignstrColors.danger,
                            lineHeight = 18.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (revealed) {
                    Text(
                        text = nsec,
                        style = TextStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = SignstrColors.textBody,
                            lineHeight = 16.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SignstrColors.bgSurface, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    )
                } else {
                    Text(
                        text = "nsec1" + "\u2022".repeat(40),
                        style = TextStyle(
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = SignstrColors.textGhost,
                            lineHeight = 16.sp
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SignstrColors.bgSurface, RoundedCornerShape(8.dp))
                            .padding(12.dp)
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
                        onClick = {
                            uk.co.signstr.app.utils.ClipboardUtil.copyWithAutoClear(
                                context, "nsec", nsec
                            )
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
