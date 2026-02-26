package uk.co.hushchip.app.ui.views.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.ui.theme.outfitFamily

@Composable
fun SplashView() {
    val goldColor = Color(0xFFB8A04A)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HushColors.bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Gold EMV chip
            Canvas(
                modifier = Modifier.size(width = 56.dp, height = 42.dp)
            ) {
                drawRoundRect(
                    color = goldColor,
                    cornerRadius = CornerRadius(8f, 8f)
                )
                val lineColor = goldColor.copy(alpha = 0.4f)
                for (i in 1..4) {
                    val y = size.height * i / 5f
                    drawLine(
                        color = lineColor,
                        start = Offset(4f, y),
                        end = Offset(size.width - 4f, y),
                        strokeWidth = 1.5f
                    )
                }
                drawLine(
                    color = lineColor,
                    start = Offset(size.width / 2f, 4f),
                    end = Offset(size.width / 2f, size.height - 4f),
                    strokeWidth = 1.5f
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "HUSH",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 36.sp,
                    letterSpacing = 8.sp,
                    color = HushColors.textBright
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "HUSHCHIP",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    letterSpacing = 5.sp,
                    color = HushColors.textFaint
                )
            )
        }
    }
}

@Preview
@Composable
private fun SplashViewPreview() {
    SplashView()
}
