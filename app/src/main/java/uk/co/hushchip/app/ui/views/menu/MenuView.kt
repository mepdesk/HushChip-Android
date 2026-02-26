package uk.co.hushchip.app.ui.views.menu

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import uk.co.hushchip.app.BackupView
import uk.co.hushchip.app.CardInformation
import uk.co.hushchip.app.FirstWelcomeView
import uk.co.hushchip.app.PinEntryView
import uk.co.hushchip.app.R
import uk.co.hushchip.app.SettingsView
import uk.co.hushchip.app.ShowCardLogs
import uk.co.hushchip.app.FactoryResetView
import uk.co.hushchip.app.ui.components.shared.HeaderAlternateRow
import uk.co.hushchip.app.ui.components.shared.InfoPopUpDialog
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.ui.theme.outfitFamily
import uk.co.hushchip.app.data.PinCodeAction
import uk.co.hushchip.app.utils.hushClickEffect
import uk.co.hushchip.app.viewmodels.SharedViewModel

@Composable
fun MenuView(
    context: Context,
    navController: NavHostController,
    viewModel: SharedViewModel,
) {
    val showInfoDialog = remember { mutableStateOf(false) }
    if (showInfoDialog.value) {
        InfoPopUpDialog(
            isOpen = showInfoDialog,
            title = R.string.cardNeedToBeScannedTitle,
            message = R.string.cardNeedToBeScannedMessage
        )
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HushColors.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = scrollState)
        ) {
            HeaderAlternateRow(
                onClick = { navController.popBackStack() },
                titleText = stringResource(R.string.settings)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                // CARD section
                SectionHeader(text = "CARD")
                SettingsGroup {
                    SettingsRow(
                        icon = "\uD83D\uDCB3",
                        title = "Card Info",
                        onClick = {
                            if (viewModel.isCardDataAvailable) {
                                navController.navigate(CardInformation)
                            } else {
                                showInfoDialog.value = true
                            }
                        }
                    )
                    GroupDivider()
                    SettingsRow(
                        icon = "\uD83D\uDD12",
                        title = "Change PIN",
                        onClick = {
                            if (viewModel.isCardDataAvailable) {
                                navController.navigate(
                                    PinEntryView(
                                        pinCodeAction = PinCodeAction.CHANGE_PIN_CODE.name,
                                        isBackupCard = false,
                                    )
                                )
                            } else {
                                showInfoDialog.value = true
                            }
                        }
                    )
                    GroupDivider()
                    SettingsRow(
                        icon = "\uD83D\uDD04",
                        title = "Backup",
                        onClick = {
                            if (viewModel.isCardDataAvailable) {
                                navController.navigate(BackupView)
                            } else {
                                showInfoDialog.value = true
                            }
                        }
                    )
                    GroupDivider()
                    SettingsRow(
                        icon = "\uD83D\uDCCB",
                        title = "View Logs",
                        onClick = {
                            if (viewModel.isCardDataAvailable) {
                                navController.navigate(ShowCardLogs)
                            } else {
                                showInfoDialog.value = true
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // APP section
                SectionHeader(text = "APP")
                SettingsGroup {
                    SettingsRow(
                        icon = "\u21BA",
                        title = "Replay Onboarding",
                        onClick = {
                            navController.navigate(FirstWelcomeView) {
                                popUpTo(0)
                            }
                        }
                    )
                    GroupDivider()
                    SettingsRow(
                        icon = "\uD83D\uDC46",
                        title = "Biometric Unlock",
                        trailingText = "SOON",
                        onClick = {}
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ABOUT section
                SectionHeader(text = "ABOUT")
                SettingsGroup {
                    SettingsRow(
                        icon = "</>",
                        title = "Open Source",
                        onClick = {
                            navController.navigate(SettingsView)
                        }
                    )
                    GroupDivider()
                    SettingsRow(
                        icon = "\u24D8",
                        title = "Version",
                        subtitle = "App v1.0 (1)",
                        onClick = {}
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // DANGER ZONE
                SectionHeader(text = "DANGER ZONE", color = HushColors.danger)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(HushColors.bgRaised, RoundedCornerShape(12.dp))
                        .border(
                            width = 1.dp,
                            color = HushColors.danger.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    SettingsRow(
                        icon = "\u26A0",
                        title = "Factory Reset",
                        titleColor = HushColors.danger,
                        iconColor = HushColors.danger,
                        onClick = {
                            navController.navigate(FactoryResetView)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String, color: androidx.compose.ui.graphics.Color = HushColors.textFaint) {
    Text(
        text = text,
        modifier = Modifier.padding(bottom = 8.dp),
        style = TextStyle(
            fontFamily = outfitFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            color = color
        )
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HushColors.bgRaised, RoundedCornerShape(12.dp))
            .border(1.dp, HushColors.border, RoundedCornerShape(12.dp))
    ) {
        content()
    }
}

@Composable
private fun GroupDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 14.dp),
        thickness = 1.dp,
        color = HushColors.border
    )
}

@Composable
private fun SettingsRow(
    icon: String,
    title: String,
    subtitle: String? = null,
    trailingText: String? = null,
    titleColor: androidx.compose.ui.graphics.Color = HushColors.textBody,
    iconColor: androidx.compose.ui.graphics.Color = HushColors.textMuted,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .hushClickEffect(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            modifier = Modifier.size(20.dp),
            style = TextStyle(
                fontSize = 14.sp,
                color = iconColor
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = titleColor
                )
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = HushColors.textMuted
                    )
                )
            }
        }
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    color = HushColors.textFaint
                )
            )
        } else {
            Text(
                text = "\u203A",
                style = TextStyle(
                    fontSize = 18.sp,
                    color = HushColors.textFaint
                )
            )
        }
    }
}
