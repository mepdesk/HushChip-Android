package uk.co.hushchip.app.ui.components.home

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import uk.co.hushchip.app.CardInformation
import uk.co.hushchip.app.MenuView
import uk.co.hushchip.app.PinEntryView
import uk.co.hushchip.app.R
import uk.co.hushchip.app.data.NfcResultCode
import uk.co.hushchip.app.data.PinCodeAction
import uk.co.hushchip.app.ui.components.shared.InfoPopUpDialog
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.ui.theme.outfitFamily
import uk.co.hushchip.app.viewmodels.SharedViewModel

@Composable
fun HomeHeaderRow(
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 8.dp)
    ) {
        // LEFT: Card-mark logo
        IconButton(
            modifier = Modifier.align(Alignment.CenterStart),
            onClick = {
                if (viewModel.isCardDataAvailable) {
                    navController.navigate(CardInformation)
                } else {
                    showInfoDialog.value = !showInfoDialog.value
                }
            },
        ) {
            Image(
                painter = painterResource(R.drawable.ic_hush_cardmark),
                contentDescription = "Card info",
                modifier = Modifier.size(28.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(HushColors.textFaint)
            )
        }

        // CENTRE: "HUSHCHIP" title
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "HUSHCHIP",
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                letterSpacing = 5.sp,
                color = HushColors.textFaint
            )
        )

        // RIGHT: Refresh + Settings icons
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (viewModel.isCardDataAvailable) {
                IconButton(
                    onClick = {
                        viewModel.setResultCodeLiveTo(NfcResultCode.NONE)
                        navController.navigate(
                            PinEntryView(
                                pinCodeAction = PinCodeAction.ENTER_PIN_CODE.name,
                                isBackupCard = false,
                            )
                        )
                    },
                ) {
                    Image(
                        painter = painterResource(R.drawable.refresh_button),
                        contentDescription = "Refresh",
                        modifier = Modifier.size(20.dp),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(HushColors.textFaint)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
            }
            IconButton(onClick = {
                navController.navigate(MenuView)
            }) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    modifier = Modifier.size(20.dp),
                    tint = HushColors.textFaint
                )
            }
        }
    }
}
