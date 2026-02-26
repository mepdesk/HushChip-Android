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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
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
            // Gold EMV chip with radial glow
            Canvas(
                modifier = Modifier.size(width = 80.dp, height = 66.dp)
            ) {
                val chipWidth = 56.dp.toPx()
                val chipHeight = 42.dp.toPx()
                val chipLeft = (size.width - chipWidth) / 2f
                val chipTop = (size.height - chipHeight) / 2f
                val chipCenterX = size.width / 2f
                val chipCenterY = size.height / 2f

                // Soft gold radial glow behind the chip
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFB4975A).copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(chipCenterX, chipCenterY),
                        radius = chipWidth * 1.2f
                    )
                )

                // Chip gradient fill
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            goldColor.copy(alpha = 0.9f),
                            goldColor.copy(alpha = 0.6f),
                        ),
                        start = Offset(chipLeft, chipTop),
                        end = Offset(chipLeft + chipWidth, chipTop + chipHeight)
                    ),
                    topLeft = Offset(chipLeft, chipTop),
                    size = androidx.compose.ui.geometry.Size(chipWidth, chipHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                // Chip border
                drawRoundRect(
                    color = goldColor.copy(alpha = 0.3f),
                    topLeft = Offset(chipLeft, chipTop),
                    size = androidx.compose.ui.geometry.Size(chipWidth, chipHeight),
                    cornerRadius = CornerRadius(8f, 8f),
                    style = Stroke(width = 1f)
                )

                val lineColor = goldColor.copy(alpha = 0.3f)
                for (i in 1..4) {
                    val y = chipTop + chipHeight * i / 5f
                    drawLine(
                        color = lineColor,
                        start = Offset(chipLeft + 4f, y),
                        end = Offset(chipLeft + chipWidth - 4f, y),
                        strokeWidth = 1.5f
                    )
                }
                drawLine(
                    color = lineColor,
                    start = Offset(chipCenterX, chipTop + 4f),
                    end = Offset(chipCenterX, chipTop + chipHeight - 4f),
                    strokeWidth = 1.5f
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            // "HUSH" text with faint gold tint
            Text(
                text = "HUSH",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 36.sp,
                    letterSpacing = 8.sp,
                    color = Color(0xFFB4975A).copy(alpha = 0.4f)
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            // "HUSHCHIP" wordmark — very subtle
            Text(
                text = "HUSHCHIP",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                    letterSpacing = 5.sp,
                    color = HushColors.textGhost
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
