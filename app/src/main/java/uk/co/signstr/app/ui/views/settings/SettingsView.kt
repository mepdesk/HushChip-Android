package uk.co.signstr.app.ui.views.settings

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import uk.co.signstr.app.crypto.Bech32
import uk.co.signstr.app.crypto.SignstrKeyStore
import uk.co.signstr.app.data.ApprovalPolicy
import uk.co.signstr.app.data.ApprovalPolicyStore
import uk.co.signstr.app.data.SignstrPreferences
import uk.co.signstr.app.ui.components.shared.GhostButton
import uk.co.signstr.app.ui.theme.SignstrColors
import uk.co.signstr.app.ui.theme.outfitFamily
import uk.co.signstr.app.utils.signstrClickEffect
import uk.co.signstr.app.viewmodels.SignstrViewModel

enum class SettingsSubScreen {
    MAIN, RELAY_CONFIG, ABOUT
}

@Composable
fun SettingsView(
    context: Context,
    viewModel: SignstrViewModel,
    onDeleteAllData: () -> Unit,
    onResetApp: () -> Unit
) {
    var subScreen by remember { mutableStateOf(SettingsSubScreen.MAIN) }

    when (subScreen) {
        SettingsSubScreen.MAIN -> SettingsMainView(
            context = context,
            viewModel = viewModel,
            onDeleteAllData = onDeleteAllData,
            onResetApp = onResetApp,
            onNavigate = { subScreen = it }
        )
        SettingsSubScreen.RELAY_CONFIG -> RelayConfigScreen(
            context = context,
            onBack = { subScreen = SettingsSubScreen.MAIN }
        )
        SettingsSubScreen.ABOUT -> AboutScreen(
            onBack = { subScreen = SettingsSubScreen.MAIN }
        )
    }
}

@Composable
private fun SettingsMainView(
    context: Context,
    viewModel: SignstrViewModel,
    onDeleteAllData: () -> Unit,
    onResetApp: () -> Unit,
    onNavigate: (SettingsSubScreen) -> Unit
) {
    val scrollState = rememberScrollState()
    var biometricsEnabled by remember { mutableStateOf(SignstrPreferences.isBiometricsEnabled(context)) }
    var notificationsEnabled by remember { mutableStateOf(SignstrPreferences.isNotificationsEnabled(context)) }
    var showPolicyPicker by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showResetConfirm by remember { mutableStateOf(false) }
    var defaultPolicy by remember { mutableStateOf(ApprovalPolicyStore.getDefaultPolicy(context)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SignstrColors.bg)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // SIGNING section
        SectionLabel("SIGNING")
        SettingsCard {
            // Default Approval Policy
            SettingsRow(
                label = "Default Approval Policy",
                value = defaultPolicy.label,
                onClick = { showPolicyPicker = true }
            )
            SettingsDivider()
            // Biometrics
            SettingsToggleRow(
                label = "Require Biometrics",
                subtitle = "Authenticate before approving",
                checked = biometricsEnabled,
                onCheckedChange = { enabled ->
                    biometricsEnabled = enabled
                    SignstrPreferences.setBiometricsEnabled(context, enabled)
                }
            )
            SettingsDivider()
            // NIP-46 Relays
            SettingsRow(
                label = "NIP-46 Relays",
                value = "${SignstrPreferences.getDefaultRelays(context).size} relays",
                onClick = { onNavigate(SettingsSubScreen.RELAY_CONFIG) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // NOTIFICATIONS section
        SectionLabel("NOTIFICATIONS")
        SettingsCard {
            SettingsToggleRow(
                label = "Signing Notifications",
                subtitle = "Alert when a request arrives",
                checked = notificationsEnabled,
                onCheckedChange = { enabled ->
                    notificationsEnabled = enabled
                    SignstrPreferences.setNotificationsEnabled(context, enabled)
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // GENERAL section
        SectionLabel("GENERAL")
        SettingsCard {
            SettingsRow(
                label = "About",
                value = "v1.0.0",
                onClick = { onNavigate(SettingsSubScreen.ABOUT) }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // DANGER ZONE
        SectionLabel("DANGER ZONE", SignstrColors.danger)

        GhostButton(
            text = "Export All Keys",
            onClick = {
                val activity = context as? FragmentActivity
                val bioEnabled = SignstrPreferences.isBiometricsEnabled(context)
                if (activity != null && bioEnabled) {
                    val canAuth = BiometricManager.from(context)
                        .canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                    if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
                        val executor = ContextCompat.getMainExecutor(context)
                        val prompt = BiometricPrompt(activity, executor,
                            object : BiometricPrompt.AuthenticationCallback() {
                                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                                    showExportDialog = true
                                }
                                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {}
                                override fun onAuthenticationFailed() {}
                            })
                        val promptInfo = BiometricPrompt.PromptInfo.Builder()
                            .setTitle("Export Keys")
                            .setSubtitle("Authenticate to view private keys")
                            .setNegativeButtonText("Cancel")
                            .build()
                        prompt.authenticate(promptInfo)
                    } else {
                        showExportDialog = true
                    }
                } else {
                    showExportDialog = true
                }
            },
            isDanger = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (!showDeleteConfirm) {
            GhostButton(
                text = "Delete All Data",
                onClick = { showDeleteConfirm = true },
                isDanger = true
            )
        } else {
            ConfirmDangerCard(
                message = "This will permanently delete all identities, connections, and event history. This cannot be undone.",
                confirmText = "Delete Everything",
                onConfirm = {
                    showDeleteConfirm = false
                    onDeleteAllData()
                },
                onCancel = { showDeleteConfirm = false }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (!showResetConfirm) {
            GhostButton(
                text = "Reset App",
                onClick = { showResetConfirm = true },
                isDanger = true
            )
        } else {
            ConfirmDangerCard(
                message = "This will delete all data and return to the onboarding screen.",
                confirmText = "Reset Everything",
                onConfirm = {
                    showResetConfirm = false
                    onResetApp()
                },
                onCancel = { showResetConfirm = false }
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // About footer
        Text(
            text = "Signstr is a product of Gridmark Technologies Ltd",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Light,
                fontSize = 10.sp,
                color = SignstrColors.textGhost
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "GPL-3.0 \u2022 github.com/nicepayments/signstr-android",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Light,
                fontSize = 10.sp,
                color = SignstrColors.textGhost
            ),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(40.dp))
    }

    // Policy picker dialog
    if (showPolicyPicker) {
        PolicyPickerDialog(
            currentPolicy = defaultPolicy,
            onSelect = { policy ->
                defaultPolicy = policy
                ApprovalPolicyStore.setDefaultPolicy(context, policy)
                showPolicyPicker = false
            },
            onDismiss = { showPolicyPicker = false }
        )
    }

    // Export all keys dialog
    if (showExportDialog) {
        ExportAllKeysDialog(
            viewModel = viewModel,
            onDismiss = { showExportDialog = false }
        )
    }
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SignstrColors.bgRaised, RoundedCornerShape(12.dp))
            .border(1.dp, SignstrColors.border, RoundedCornerShape(12.dp))
    ) {
        Column(content = content)
    }
}

@Composable
private fun SettingsRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .signstrClickEffect(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = SignstrColors.textBody
            )
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Light,
                fontSize = 12.sp,
                color = SignstrColors.textFaint
            )
        )
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    subtitle: String = "",
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = SignstrColors.textBody
                )
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 10.sp,
                        color = SignstrColors.textGhost
                    )
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = SignstrColors.textBright,
                checkedTrackColor = SignstrColors.borderHover,
                uncheckedThumbColor = SignstrColors.textFaint,
                uncheckedTrackColor = SignstrColors.bgSurface,
                uncheckedBorderColor = SignstrColors.border
            )
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        color = SignstrColors.border,
        thickness = 0.5.dp,
        modifier = Modifier.padding(horizontal = 14.dp)
    )
}

@Composable
private fun ConfirmDangerCard(
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SignstrColors.dangerBg, RoundedCornerShape(12.dp))
            .border(1.dp, SignstrColors.dangerBorder, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = message,
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp,
                    color = SignstrColors.danger,
                    lineHeight = 18.sp
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GhostButton(
                    text = "Cancel",
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                )
                GhostButton(
                    text = confirmText,
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    isDanger = true
                )
            }
        }
    }
}

@Composable
private fun PolicyPickerDialog(
    currentPolicy: ApprovalPolicy,
    onSelect: (ApprovalPolicy) -> Unit,
    onDismiss: () -> Unit
) {
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
                    text = "DEFAULT APPROVAL POLICY",
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

                for (policy in ApprovalPolicy.entries) {
                    val isSelected = policy == currentPolicy
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .signstrClickEffect(onClick = { onSelect(policy) })
                            .background(
                                if (isSelected) SignstrColors.border else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Text(
                                text = policy.label,
                                style = TextStyle(
                                    fontFamily = outfitFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (isSelected) SignstrColors.textBright else SignstrColors.textBody
                                )
                            )
                            Text(
                                text = policy.description,
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

@Composable
private fun ExportAllKeysDialog(
    viewModel: SignstrViewModel,
    onDismiss: () -> Unit
) {
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
                    text = "EXPORT ALL KEYS",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        letterSpacing = 4.sp,
                        color = SignstrColors.danger
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                for (identity in viewModel.identities) {
                    val nsec = viewModel.getNsec(identity.id) ?: "Unable to load"
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = identity.name.uppercase(),
                            style = TextStyle(
                                fontFamily = outfitFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 9.sp,
                                letterSpacing = 2.sp,
                                color = SignstrColors.textGhost
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SignstrColors.bgSurface, RoundedCornerShape(8.dp))
                                .border(1.dp, SignstrColors.border, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = nsec,
                                style = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 10.sp,
                                    color = SignstrColors.danger,
                                    lineHeight = 14.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                GhostButton(text = "Close", onClick = onDismiss)
            }
        }
    }
}

// Relay Config sub-screen
@Composable
private fun RelayConfigScreen(
    context: Context,
    onBack: () -> Unit
) {
    var relays by remember { mutableStateOf(SignstrPreferences.getDefaultRelays(context)) }
    var newRelay by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SignstrColors.bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\u2190",
                style = TextStyle(
                    fontSize = 20.sp,
                    color = SignstrColors.textMuted
                ),
                modifier = Modifier.signstrClickEffect(onClick = onBack)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "NIP-46 RELAYS",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    letterSpacing = 4.sp,
                    color = SignstrColors.textFaint
                )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        for ((index, relay) in relays.withIndex()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SignstrColors.bgRaised, RoundedCornerShape(8.dp))
                    .border(1.dp, SignstrColors.border, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = relay,
                    modifier = Modifier.weight(1f),
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = SignstrColors.textBody
                    )
                )
                Text(
                    text = "REMOVE",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 9.sp,
                        letterSpacing = 1.sp,
                        color = SignstrColors.danger
                    ),
                    modifier = Modifier.signstrClickEffect(onClick = {
                        relays = relays.toMutableList().also { it.removeAt(index) }
                        SignstrPreferences.saveDefaultRelays(context, relays)
                    })
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add relay
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(SignstrColors.bgSurface, RoundedCornerShape(8.dp))
                    .border(1.dp, SignstrColors.border, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                androidx.compose.foundation.text.BasicTextField(
                    value = newRelay,
                    onValueChange = { newRelay = it },
                    textStyle = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = SignstrColors.textBright
                    ),
                    cursorBrush = androidx.compose.ui.graphics.SolidColor(SignstrColors.textMuted),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        Box {
                            if (newRelay.isEmpty()) {
                                Text(
                                    text = "wss://relay.example.com",
                                    style = TextStyle(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Light,
                                        fontSize = 12.sp,
                                        color = SignstrColors.textGhost
                                    )
                                )
                            }
                            inner()
                        }
                    }
                )
            }
            GhostButton(
                text = "Add",
                onClick = {
                    val trimmed = newRelay.trim()
                    if (trimmed.startsWith("wss://") && trimmed !in relays) {
                        relays = relays + trimmed
                        SignstrPreferences.saveDefaultRelays(context, relays)
                        newRelay = ""
                    }
                },
                modifier = Modifier.width(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        GhostButton(
            text = "Reset to Defaults",
            onClick = {
                relays = SignstrPreferences.DEFAULT_RELAYS
                SignstrPreferences.saveDefaultRelays(context, relays)
            }
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// About sub-screen
@Composable
private fun AboutScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SignstrColors.bg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "\u2190",
                style = TextStyle(
                    fontSize = 20.sp,
                    color = SignstrColors.textMuted
                ),
                modifier = Modifier.signstrClickEffect(onClick = onBack)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "ABOUT",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    letterSpacing = 4.sp,
                    color = SignstrColors.textFaint
                )
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "SIGNSTR",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                letterSpacing = 6.sp,
                color = SignstrColors.textMuted
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Version 1.0.0",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Light,
                fontSize = 13.sp,
                color = SignstrColors.textFaint
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Signstr is a NIP-46 remote signer for Nostr. It keeps your private keys secure on your device and signs events on behalf of connected Nostr clients.",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Light,
                fontSize = 13.sp,
                color = SignstrColors.textBody,
                lineHeight = 20.sp
            ),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        InfoRow("Developer", "Gridmark Technologies Ltd")
        InfoRow("License", "GPL-3.0")
        InfoRow("Source", "github.com/nicepayments/signstr-android")
        InfoRow("Protocol", "NIP-46 (Nostr Connect)")

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 9.sp,
                letterSpacing = 2.sp,
                color = SignstrColors.textGhost
            ),
            modifier = Modifier.width(100.dp)
        )
        Text(
            text = value,
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Light,
                fontSize = 13.sp,
                color = SignstrColors.textBody
            )
        )
    }
}

@Composable
private fun SectionLabel(text: String, color: Color = SignstrColors.textFaint) {
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
                color = color
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
