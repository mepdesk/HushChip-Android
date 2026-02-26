package uk.co.hushchip.app.ui.views.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.hushchip.app.ui.components.shared.WelcomeViewTitle
import uk.co.hushchip.app.ui.theme.HushColors

@Composable
fun SplashView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HushColors.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            WelcomeViewTitle(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 64.dp)
            )
        }
    }
}


@Preview
@Composable
private fun SplashViewPreview() {
    SplashView()
}