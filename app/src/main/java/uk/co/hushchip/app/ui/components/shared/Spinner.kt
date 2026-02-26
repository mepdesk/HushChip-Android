package uk.co.hushchip.app.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uk.co.hushchip.app.ui.theme.HushColors

@Composable
fun Spinner() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HushColors.bg),
    ) {
        SpinnerComponent()
    }
}