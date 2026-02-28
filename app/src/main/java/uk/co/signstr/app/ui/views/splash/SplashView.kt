package uk.co.signstr.app.ui.views.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.signstr.app.ui.theme.SignstrColors
import uk.co.signstr.app.ui.theme.outfitFamily

@Composable
fun SplashView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SignstrColors.bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "SIGNSTR.",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 32.sp,
                    letterSpacing = 6.sp,
                    color = SignstrColors.textBright
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "NOSTR REMOTE SIGNER",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 9.sp,
                    letterSpacing = 4.sp,
                    color = SignstrColors.textGhost
                )
            )
        }
    }
}
