package uk.co.signstr.app.ui.views.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.signstr.app.ui.theme.SignstrColors
import uk.co.signstr.app.ui.theme.outfitFamily

@Composable
fun SplashView() {
    // Animate from 0 to 1 over 2.5 seconds
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2500, easing = FastOutSlowInEasing)
        )
    }

    val inkColor = Color(0xEBB9B9C3) // rgba(185,185,195,0.92)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SignstrColors.bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Signature canvas
            Canvas(
                modifier = Modifier
                    .width(280.dp)
                    .height(100.dp)
            ) {
                val w = size.width
                val h = size.height

                // Scale factor to fit the signature path into the canvas
                val sc = w / 60f
                val ox = w * 0.08f
                val oy = h * 0.1f

                // Build the signature path — cubic bezier segments from the HTML reference
                // These coordinates represent "Signstr." in a flowing signature style
                val segments = listOf(
                    // S flourish
                    floatArrayOf(21f,8f, 19f,7f, 16.5f,7f, 14.5f,8f),
                    floatArrayOf(14.5f,8f, 11.5f,9.5f, 10f,12f, 10.5f,14.5f),
                    floatArrayOf(10.5f,14.5f, 11f,16.5f, 13.5f,17.5f, 16.5f,17.5f),
                    floatArrayOf(16.5f,17.5f, 19.5f,17.5f, 22.5f,18.5f, 22.5f,21f),
                    floatArrayOf(22.5f,21f, 22.5f,23.5f, 19.5f,25.5f, 16.5f,26f),
                    floatArrayOf(16.5f,26f, 14.5f,26.5f, 13f,25.5f, 14f,24f),
                    floatArrayOf(14f,24f, 15.5f,22.5f, 18f,21.5f, 20f,21.5f),
                    floatArrayOf(20f,21.5f, 22f,21.5f, 23.5f,22f, 24f,23f),
                    // ignstr body
                    floatArrayOf(24f,23f, 24.5f,24f, 25f,24.2f, 25.5f,23.5f),
                    floatArrayOf(25.5f,23.5f, 26.5f,22.5f, 28f,22.5f, 29f,23.5f),
                    floatArrayOf(29f,23.5f, 30f,24.5f, 30f,27f, 29.5f,29f),
                    floatArrayOf(29.5f,29f, 29f,31f, 27f,31f, 27f,29f),
                    floatArrayOf(27f,29f, 27f,27.5f, 29f,25.5f, 31f,24f),
                    floatArrayOf(31f,24f, 32f,23f, 33.5f,22.5f, 34.5f,23.5f),
                    floatArrayOf(34.5f,23.5f, 35f,24.5f, 35.5f,24f, 36f,23.5f),
                    // t body
                    floatArrayOf(36f,23.5f, 36.5f,23f, 38f,21.5f, 39f,21.5f),
                    // r
                    floatArrayOf(39f,21.5f, 40f,21.5f, 39.5f,23.2f, 38.2f,23.8f),
                    floatArrayOf(38.2f,23.8f, 37.2f,24.2f, 39.2f,24.5f, 40f,23.5f),
                    // tail
                    floatArrayOf(40f,23.5f, 40.2f,22.2f, 40.8f,20.5f, 41f,19.5f),
                    floatArrayOf(41f,19.5f, 41.2f,19f, 41.5f,19.2f, 41.5f,20f),
                    floatArrayOf(41.5f,20f, 41.5f,21.5f, 42f,23.5f, 42.5f,24.5f),
                    floatArrayOf(42.5f,24.5f, 43f,23.5f, 43.5f,22.5f, 44.2f,22.2f),
                    floatArrayOf(44.2f,22.2f, 45f,22f, 45.8f,22.3f, 46.5f,23f),
                    floatArrayOf(46.5f,23f, 48f,23.5f, 50.5f,23f, 53f,22.5f)
                )

                val signaturePath = Path()
                for ((idx, seg) in segments.withIndex()) {
                    val x0 = seg[0] * sc + ox
                    val y0 = seg[1] * sc + oy
                    val x1 = seg[2] * sc + ox
                    val y1 = seg[3] * sc + oy
                    val x2 = seg[4] * sc + ox
                    val y2 = seg[5] * sc + oy
                    val x3 = seg[6] * sc + ox
                    val y3 = seg[7] * sc + oy
                    if (idx == 0) signaturePath.moveTo(x0, y0)
                    signaturePath.cubicTo(x1, y1, x2, y2, x3, y3)
                }

                // i dot
                val iDotX = 25.5f * sc + ox
                val iDotY = 19.5f * sc + oy

                // t cross
                val tCrossX1 = 39f * sc + ox
                val tCrossY1 = 20.5f * sc + oy
                val tCrossX2 = 43.5f * sc + ox
                val tCrossY2 = 19.8f * sc + oy

                // Full stop
                val dotX = 55f * sc + ox
                val dotY = 23f * sc + oy

                // Measure the main path
                val pathMeasure = PathMeasure()
                pathMeasure.setPath(signaturePath, false)
                val totalLen = pathMeasure.length

                val prog = progress.value

                // Phase 1: main stroke (0 to 0.75 of progress)
                // Phase 2: i dot (0.78)
                // Phase 3: t cross (0.82-0.88)
                // Phase 4: full stop (0.95)

                // Draw main signature stroke
                val mainProgress = (prog / 0.75f).coerceIn(0f, 1f)
                if (mainProgress > 0f) {
                    val partialPath = Path()
                    pathMeasure.getSegment(0f, totalLen * mainProgress, partialPath, true)

                    // Variable stroke width simulation: draw with varying alpha/width
                    val baseWidth = sc * 0.35f
                    val thickWidth = sc * 0.85f
                    // S body is thick, tail thins out
                    val strokeWidth = if (mainProgress < 0.35f) {
                        thickWidth
                    } else {
                        thickWidth - (thickWidth - baseWidth) * ((mainProgress - 0.35f) / 0.65f)
                    }

                    drawPath(
                        path = partialPath,
                        color = inkColor,
                        style = Stroke(
                            width = strokeWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // i dot
                if (prog > 0.78f) {
                    val dotAlpha = ((prog - 0.78f) / 0.04f).coerceIn(0f, 1f)
                    drawCircle(
                        color = inkColor.copy(alpha = dotAlpha),
                        radius = sc * 0.2f,
                        center = Offset(iDotX, iDotY)
                    )
                }

                // t cross
                val tProgress = ((prog - 0.82f) / 0.06f).coerceIn(0f, 1f)
                if (tProgress > 0f) {
                    val crossEndX = tCrossX1 + (tCrossX2 - tCrossX1) * tProgress
                    val crossEndY = tCrossY1 + (tCrossY2 - tCrossY1) * tProgress
                    drawLine(
                        color = inkColor,
                        start = Offset(tCrossX1, tCrossY1),
                        end = Offset(crossEndX, crossEndY),
                        strokeWidth = sc * 0.25f,
                        cap = StrokeCap.Round
                    )
                }

                // Full stop dot
                if (prog > 0.95f) {
                    val stopAlpha = ((prog - 0.95f) / 0.05f).coerceIn(0f, 1f)
                    drawCircle(
                        color = inkColor.copy(alpha = stopAlpha),
                        radius = sc * 0.28f,
                        center = Offset(dotX, dotY)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tagline appears after signature completes
            val taglineAlpha = ((progress.value - 0.9f) / 0.1f).coerceIn(0f, 1f)
            Text(
                text = "your keys. your identity.",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Light,
                    fontSize = 11.sp,
                    letterSpacing = 0.4.sp,
                    color = SignstrColors.textFaint.copy(alpha = taglineAlpha)
                )
            )
        }
    }
}
