package uk.co.hushchip.app.ui.views.import

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import uk.co.hushchip.app.R
import uk.co.hushchip.app.ui.components.import.InputField
import uk.co.hushchip.app.ui.components.shared.GifImage
import uk.co.hushchip.app.ui.components.shared.HushButton
import uk.co.hushchip.app.ui.components.shared.TitleTextField
import uk.co.hushchip.app.ui.theme.SatoPurple
import uk.co.hushchip.app.viewmodels.SharedViewModel

@Composable
fun ImportHome(
    context: Context,
    navController: NavHostController,
    viewModel: SharedViewModel,
    curValueLabel: MutableState<String>,
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        TitleTextField(
            title = R.string.congratulations,
            text = R.string.generateSuccessful
        )
        Spacer(modifier = Modifier.height(8.dp))
        InputField(
            isEditable = false,
            curValue = curValueLabel,
            containerColor = SatoPurple.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(20.dp))
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
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center
        ) {
            //Home
            HushButton(
                onClick = {
                    //onClick(ImportViewItems.HOME, null)
                    navController.popBackStack()
                    navController.popBackStack()
                },
                text = R.string.home
            )
        }
    }
}