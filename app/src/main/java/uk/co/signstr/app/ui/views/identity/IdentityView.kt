package uk.co.signstr.app.ui.views.identity

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
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
import uk.co.signstr.app.crypto.NIP44
import uk.co.signstr.app.data.SignstrIdentity
import uk.co.signstr.app.data.SignstrPreferences
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
    var showRenameDialog by remember { mutableStateOf(false) }
    var showBackupNsec by remember { mutableStateOf(false) }
    var showSafeKindsEditor by remember { mutableStateOf(false) }

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

            // Identity name (tappable to rename)
            Text(
                text = activeIdentity.name,
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 20.sp,
                    color = SignstrColors.textBright,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.signstrClickEffect(onClick = { showRenameDialog = true })
            )
            Text(
                text = "Tap to rename",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 10.sp,
                    color = SignstrColors.textGhost
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

            // SIGNING POLICY section
            SectionLabel("SIGNING POLICY")
            SigningPolicyCard(
                identity = activeIdentity,
                viewModel = viewModel,
                onEditSafeKinds = { showSafeKindsEditor = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GhostButton(
                    text = "Backup nsec",
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
                                            showBackupNsec = true
                                        }
                                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {}
                                        override fun onAuthenticationFailed() {}
                                    })
                                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                    .setTitle("Backup Key")
                                    .setSubtitle("Authenticate to view nsec")
                                    .setNegativeButtonText("Cancel")
                                    .build()
                                prompt.authenticate(promptInfo)
                            } else {
                                showBackupNsec = true
                            }
                        } else {
                            showBackupNsec = true
                        }
                    },
                    modifier = Modifier.weight(1f),
                    isDanger = true
                )
                GhostButton(
                    text = "Add Identity",
                    onClick = onCreateIdentity,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // NostrKey Card
            NostrKeyCard(context = context)

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Rename dialog
    if (showRenameDialog && activeIdentity != null) {
        RenameDialog(
            currentName = activeIdentity.name,
            onRename = { newName ->
                viewModel.renameIdentity(activeIdentity, newName)
                showRenameDialog = false
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    // Backup nsec dialog
    if (showBackupNsec && activeIdentity != null) {
        val nsec = viewModel.getNsec(activeIdentity.id) ?: ""
        BackupNsecDialog(
            nsec = nsec,
            context = context,
            onDismiss = { showBackupNsec = false }
        )
    }

    // Safe kinds editor
    if (showSafeKindsEditor && activeIdentity != null) {
        SafeKindsEditorDialog(
            identity = activeIdentity,
            onSave = { updatedKinds ->
                val updated = activeIdentity.copy(safeKinds = updatedKinds)
                viewModel.updateIdentity(updated)
                showSafeKindsEditor = false
            },
            onDismiss = { showSafeKindsEditor = false }
        )
    }
}

@Composable
private fun SigningPolicyCard(
    identity: SignstrIdentity,
    viewModel: SignstrViewModel,
    onEditSafeKinds: () -> Unit
) {
    var autoApprove by remember(identity) { mutableStateOf(identity.autoApproveEnabled) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SignstrColors.bgRaised, RoundedCornerShape(12.dp))
            .border(1.dp, SignstrColors.border, RoundedCornerShape(12.dp))
    ) {
        Column {
            // Auto-approve safe kinds toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Auto-approve safe kinds",
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = SignstrColors.textBody
                        )
                    )
                    Text(
                        text = identity.safeKinds.joinToString(", "),
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 10.sp,
                            color = SignstrColors.textGhost
                        )
                    )
                }
                Switch(
                    checked = autoApprove,
                    onCheckedChange = { enabled ->
                        autoApprove = enabled
                        val updated = identity.copy(autoApproveEnabled = enabled)
                        viewModel.updateIdentity(updated)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = SignstrColors.textBright,
                        checkedTrackColor = SignstrColors.borderHover,
                        uncheckedThumbColor = SignstrColors.textFaint,
                        uncheckedTrackColor = SignstrColors.bgSurface,
                        uncheckedBorderColor = SignstrColors.border
                    )
                )
            }

            HorizontalDivider(
                color = SignstrColors.border,
                thickness = 0.5.dp,
                modifier = Modifier.padding(horizontal = 14.dp)
            )

            // Edit safe kinds
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .signstrClickEffect(onClick = onEditSafeKinds)
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Edit Safe Event Types",
                    modifier = Modifier.weight(1f),
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = SignstrColors.textBody
                    )
                )
                Text(
                    text = "${identity.safeKinds.size} kinds",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = SignstrColors.textFaint
                    )
                )
            }
        }
    }
}

@Composable
private fun RenameDialog(
    currentName: String,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(currentName) }

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
                    text = "RENAME IDENTITY",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        letterSpacing = 4.sp,
                        color = SignstrColors.textFaint
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
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
                        .padding(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GhostButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    )
                    GhostButton(
                        text = "Save",
                        onClick = {
                            val trimmed = name.trim()
                            if (trimmed.isNotEmpty()) onRename(trimmed)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BackupNsecDialog(
    nsec: String,
    context: Context,
    onDismiss: () -> Unit
) {
    var revealed by remember { mutableStateOf(false) }

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
                    text = "BACKUP NSEC",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        letterSpacing = 4.sp,
                        color = SignstrColors.danger
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SignstrColors.bgSurface, RoundedCornerShape(8.dp))
                        .border(1.dp, SignstrColors.border, RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = if (revealed) nsec else "nsec1" + "\u2022".repeat(40),
                        style = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = if (revealed) SignstrColors.danger else SignstrColors.textGhost,
                            lineHeight = 16.sp
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
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
                Spacer(modifier = Modifier.height(12.dp))
                GhostButton(text = "Close", onClick = onDismiss)
            }
        }
    }
}

@Composable
private fun SafeKindsEditorDialog(
    identity: SignstrIdentity,
    onSave: (List<Int>) -> Unit,
    onDismiss: () -> Unit
) {
    var kinds by remember { mutableStateOf(identity.safeKinds.toMutableList()) }
    var newKindText by remember { mutableStateOf("") }

    val kindLabels = mapOf(
        0 to "Profile metadata",
        1 to "Short note",
        3 to "Contact list",
        4 to "Encrypted DM",
        7 to "Reaction",
        10000 to "Mute list",
        10001 to "Pin list",
        10002 to "Relay list",
        22242 to "Auth challenge"
    )

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
                    text = "SAFE EVENT TYPES",
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

                for (kind in kinds.sorted()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Kind $kind",
                                style = TextStyle(
                                    fontFamily = outfitFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = SignstrColors.textBody
                                )
                            )
                            kindLabels[kind]?.let { label ->
                                Text(
                                    text = label,
                                    style = TextStyle(
                                        fontFamily = outfitFamily,
                                        fontWeight = FontWeight.Light,
                                        fontSize = 10.sp,
                                        color = SignstrColors.textGhost
                                    )
                                )
                            }
                        }
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
                                kinds = kinds.toMutableList().also { it.remove(kind) }
                            })
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Add kind
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = newKindText,
                        onValueChange = { newKindText = it.filter { c -> c.isDigit() } },
                        textStyle = TextStyle(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = SignstrColors.textBright
                        ),
                        cursorBrush = SolidColor(SignstrColors.textMuted),
                        modifier = Modifier
                            .weight(1f)
                            .background(SignstrColors.bgSurface, RoundedCornerShape(8.dp))
                            .border(1.dp, SignstrColors.border, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        decorationBox = { inner ->
                            Box {
                                if (newKindText.isEmpty()) {
                                    Text(
                                        text = "Kind number",
                                        style = TextStyle(
                                            fontFamily = outfitFamily,
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
                    GhostButton(
                        text = "Add",
                        onClick = {
                            val kind = newKindText.toIntOrNull()
                            if (kind != null && kind !in kinds) {
                                kinds = (kinds + kind).toMutableList()
                                newKindText = ""
                            }
                        },
                        modifier = Modifier.width(80.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GhostButton(
                        text = "Reset",
                        onClick = {
                            kinds = SignstrIdentity.DEFAULT_SAFE_KINDS.toMutableList()
                        },
                        modifier = Modifier.weight(1f)
                    )
                    GhostButton(
                        text = "Save",
                        onClick = { onSave(kinds.sorted()) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun NostrKeyCard(context: Context) {
    SectionLabel("HARDWARE")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SignstrColors.bgRaised, RoundedCornerShape(12.dp))
            .border(1.dp, SignstrColors.border, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Phone icon drawn with text
                Text(
                    text = "\uD83D\uDCF1",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "GO AIR-GAPPED",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        letterSpacing = 4.sp,
                        color = SignstrColors.textBright
                    )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Your key lives on this device. Want it off? NostrKey card stores your nsec in a secure element. Your key never touches your phone again. Tap to sign. Nothing to hack.",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 12.sp,
                    color = SignstrColors.textMuted,
                    lineHeight = 18.sp
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "GBP 14.99",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = SignstrColors.textBody
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            GhostButton(
                text = "Learn More",
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://signstr.com/card"))
                    context.startActivity(intent)
                }
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
