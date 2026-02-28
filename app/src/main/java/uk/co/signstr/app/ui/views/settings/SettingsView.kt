package uk.co.signstr.app.ui.views.settings

import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.signstr.app.ui.components.shared.GhostButton
import uk.co.signstr.app.ui.components.shared.IdentityPicker
import uk.co.signstr.app.ui.theme.SignstrColors
import uk.co.signstr.app.ui.theme.outfitFamily
import uk.co.signstr.app.utils.signstrClickEffect
import uk.co.signstr.app.viewmodels.SignstrViewModel

@Composable
fun SettingsView(
    context: Context,
    viewModel: SignstrViewModel,
    onExportNsec: () -> Unit
) {
    val scrollState = rememberScrollState()
    val activeIdentity = viewModel.activeIdentity.value
    var autoApprove by remember(activeIdentity) {
        mutableStateOf(activeIdentity?.autoApproveEnabled ?: true)
    }

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
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // SIGNING POLICIES section
            SectionLabel("SIGNING POLICIES")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(12.dp), ambientColor = Color.Black.copy(alpha = 0.3f))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(SignstrColors.bgRaised, Color(0xFF0C0C0E))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.02f))
                        ),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 14.dp),
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
                                text = "0, 3, 10000, 10001, 10002, 22242",
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
                                // Update identity in preferences
                                val identity = viewModel.activeIdentity.value ?: return@Switch
                                val updated = identity.copy(autoApproveEnabled = enabled)
                                val ctx = context
                                val identities = viewModel.identities.toMutableList()
                                val idx = identities.indexOfFirst { it.id == identity.id }
                                if (idx >= 0) {
                                    identities[idx] = updated
                                    viewModel.identities.clear()
                                    viewModel.identities.addAll(identities)
                                    viewModel.activeIdentity.value = updated
                                    uk.co.signstr.app.data.SignstrPreferences.saveIdentities(ctx, identities)
                                }
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
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // APP section
            SectionLabel("APP")
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(12.dp), ambientColor = Color.Black.copy(alpha = 0.3f))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(SignstrColors.bgRaised, Color(0xFF0C0C0E))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        Brush.verticalGradient(
                            colors = listOf(Color.White.copy(alpha = 0.06f), Color.White.copy(alpha = 0.02f))
                        ),
                        RoundedCornerShape(12.dp)
                    )
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Version",
                            modifier = Modifier.weight(1f),
                            style = TextStyle(
                                fontFamily = outfitFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = SignstrColors.textBody
                            )
                        )
                        Text(
                            text = "1.0.0",
                            style = TextStyle(
                                fontFamily = outfitFamily,
                                fontWeight = FontWeight.Light,
                                fontSize = 14.sp,
                                color = SignstrColors.textFaint
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DANGER ZONE
            SectionLabel("DANGER ZONE", SignstrColors.danger)

            GhostButton(
                text = "Export nsec",
                onClick = onExportNsec,
                isDanger = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (activeIdentity != null) {
                GhostButton(
                    text = "Delete Identity",
                    onClick = {
                        viewModel.deleteIdentity(activeIdentity)
                        Toast.makeText(context, "Identity deleted", Toast.LENGTH_SHORT).show()
                    },
                    isDanger = true
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // About
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
