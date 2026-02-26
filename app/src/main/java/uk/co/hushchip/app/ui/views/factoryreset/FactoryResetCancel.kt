package uk.co.hushchip.app.ui.views.factoryreset

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import uk.co.hushchip.app.HomeView
import uk.co.hushchip.app.R
import uk.co.hushchip.app.ui.components.settings.ResetCardTextField
import uk.co.hushchip.app.ui.components.shared.GifImage
import uk.co.hushchip.app.ui.components.shared.HushButton
import uk.co.hushchip.app.viewmodels.SharedViewModel

@Composable
fun FactoryResetCancel(
    context: Context,
    navController: NavHostController,
    viewModel: SharedViewModel,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ResetCardTextField(
            title = R.string.blankTextField,
            text = R.string.resetCancelled,
        )
        Spacer(modifier = Modifier.height(24.dp))

        //viewModel.getCardStatus()?.pin0RemainingCounter.let { pinRemaining ->
        viewModel.getCardStatus()?.let { status ->
            if (status.protocolVersion >= 2) {
                viewModel.resultCodeLive.triesLeft?.let { pinRemaining ->
                    Text(
                        text = stringResource(id = R.string.pinRemaining) + "$pinRemaining",
                        style = TextStyle(
                            color = Color.Red,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            GifImage(
                modifier = Modifier
                    .size(300.dp)
                    .align(Alignment.Center),
                image = R.drawable.vault
            )
        }
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        HushButton(
            modifier = Modifier
                .padding(
                    horizontal = 6.dp
                ),
            onClick = {
                navController.navigate(HomeView) {
                    popUpTo(0)
                }
            },
            text = R.string.home,
        )
        Spacer(modifier = Modifier.height(35.dp))
    }
}