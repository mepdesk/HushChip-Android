package uk.co.signstr.app.ui.views.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.signstr.app.ui.components.shared.GhostButton
import uk.co.signstr.app.ui.theme.SignstrColors
import uk.co.signstr.app.ui.theme.outfitFamily
import kotlinx.coroutines.launch

data class OnboardingPage(val title: String, val body: String, val buttonText: String)

private val pages = listOf(
    OnboardingPage(
        title = "Your Nostr identity.\nSecured.",
        body = "Signstr keeps your nsec encrypted in one place. No more pasting it into every app.",
        buttonText = "NEXT"
    ),
    OnboardingPage(
        title = "Connect your\nfavourite clients.",
        body = "Damus, Primal, and other Nostr clients can request signatures without ever seeing your nsec.",
        buttonText = "NEXT"
    ),
    OnboardingPage(
        title = "Biometrics approve\nevery signature.",
        body = "Set approval policies per app. Always ask, trust for a session, or auto-approve by event kind.",
        buttonText = "GET STARTED"
    )
)

@Composable
fun OnboardingView(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val iconColor = SignstrColors.textMuted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SignstrColors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.15f))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(0.65f)
        ) { page ->
            val p = pages[page]
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Illustration
                Canvas(
                    modifier = Modifier.size(120.dp)
                ) {
                    when (page) {
                        0 -> drawShieldKey(iconColor.hashCode().toLong())
                        1 -> drawConnectedNodes(iconColor.hashCode().toLong())
                        2 -> drawFingerprint(iconColor.hashCode().toLong())
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = p.title,
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp,
                        letterSpacing = 0.5.sp,
                        color = SignstrColors.textBright,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = p.body,
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = SignstrColors.textMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                )
            }
        }

        // Dot indicators
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 24.dp)
        ) {
            repeat(pages.size) { i ->
                val isActive = i == pagerState.currentPage
                Box(
                    modifier = Modifier
                        .then(
                            if (isActive) Modifier
                                .width(24.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                            else Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                        )
                        .background(
                            if (isActive) SignstrColors.textMuted else SignstrColors.textGhost
                        )
                )
            }
        }

        GhostButton(
            text = pages[pagerState.currentPage].buttonText,
            onClick = {
                if (pagerState.currentPage < pages.size - 1) {
                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                } else {
                    onComplete()
                }
            }
        )

        Spacer(modifier = Modifier.height(40.dp))
    }
}

// Page 1: Shield with key icon
private fun DrawScope.drawShieldKey(seed: Long) {
    val color = SignstrColors.textMuted
    val cx = size.width / 2f
    val cy = size.height / 2f
    val s = size.width * 0.35f
    val strokeW = size.width * 0.025f

    // Shield outline
    val shieldPath = Path().apply {
        moveTo(cx, cy - s * 1.1f)
        cubicTo(cx - s * 0.8f, cy - s * 0.9f, cx - s * 1.0f, cy - s * 0.3f, cx - s * 0.9f, cy + s * 0.1f)
        cubicTo(cx - s * 0.7f, cy + s * 0.8f, cx, cy + s * 1.2f, cx, cy + s * 1.2f)
        cubicTo(cx, cy + s * 1.2f, cx + s * 0.7f, cy + s * 0.8f, cx + s * 0.9f, cy + s * 0.1f)
        cubicTo(cx + s * 1.0f, cy - s * 0.3f, cx + s * 0.8f, cy - s * 0.9f, cx, cy - s * 1.1f)
        close()
    }
    drawPath(shieldPath, color, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Key inside shield
    val kr = s * 0.25f
    val ky = cy - s * 0.15f
    // Key head (circle)
    drawCircle(color, radius = kr, center = Offset(cx, ky), style = Stroke(width = strokeW))
    // Key shaft
    drawLine(color, start = Offset(cx, ky + kr), end = Offset(cx, cy + s * 0.55f), strokeWidth = strokeW, cap = StrokeCap.Round)
    // Key teeth
    drawLine(color, start = Offset(cx, cy + s * 0.3f), end = Offset(cx + s * 0.18f, cy + s * 0.3f), strokeWidth = strokeW, cap = StrokeCap.Round)
    drawLine(color, start = Offset(cx, cy + s * 0.45f), end = Offset(cx + s * 0.14f, cy + s * 0.45f), strokeWidth = strokeW, cap = StrokeCap.Round)
}

// Page 2: Document with connected nodes
private fun DrawScope.drawConnectedNodes(seed: Long) {
    val color = SignstrColors.textMuted
    val cx = size.width / 2f
    val cy = size.height / 2f
    val strokeW = size.width * 0.025f
    val nodeR = size.width * 0.06f

    // Central document
    val docW = size.width * 0.3f
    val docH = size.height * 0.4f
    drawRoundRect(
        color = color,
        topLeft = Offset(cx - docW / 2, cy - docH / 2),
        size = Size(docW, docH),
        cornerRadius = CornerRadius(size.width * 0.03f),
        style = Stroke(width = strokeW)
    )
    // Doc lines
    val lineLeft = cx - docW * 0.3f
    val lineRight = cx + docW * 0.3f
    for (i in 0..2) {
        val ly = cy - docH * 0.15f + i * docH * 0.15f
        drawLine(color.copy(alpha = 0.5f), Offset(lineLeft, ly), Offset(lineRight, ly), strokeW * 0.7f, cap = StrokeCap.Round)
    }

    // Nodes around
    val nodes = listOf(
        Offset(cx - size.width * 0.35f, cy - size.height * 0.25f),
        Offset(cx + size.width * 0.35f, cy - size.height * 0.2f),
        Offset(cx - size.width * 0.3f, cy + size.height * 0.3f),
        Offset(cx + size.width * 0.32f, cy + size.height * 0.28f)
    )
    for (node in nodes) {
        // Line from doc center to node
        drawLine(color.copy(alpha = 0.3f), Offset(cx, cy), node, strokeW * 0.6f, cap = StrokeCap.Round)
        // Node circle
        drawCircle(color, radius = nodeR, center = node, style = Stroke(width = strokeW))
        drawCircle(color.copy(alpha = 0.3f), radius = nodeR * 0.4f, center = node)
    }
}

// Page 3: Fingerprint/biometric icon
private fun DrawScope.drawFingerprint(seed: Long) {
    val color = SignstrColors.textMuted
    val cx = size.width / 2f
    val cy = size.height / 2f
    val strokeW = size.width * 0.022f

    // Concentric arcs to represent fingerprint ridges
    val baseR = size.width * 0.08f
    for (i in 1..5) {
        val r = baseR * i
        val startAngle = -160f + i * 10f
        val sweepAngle = 140f - i * 5f
        drawArc(
            color = color.copy(alpha = 0.9f - i * 0.1f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2, r * 2),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
    }

    // Additional ridges offset
    for (i in 1..4) {
        val r = baseR * i + baseR * 0.5f
        val startAngle = 20f - i * 5f
        val sweepAngle = 120f - i * 8f
        drawArc(
            color = color.copy(alpha = 0.7f - i * 0.1f),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(cx - r, cy - r),
            size = Size(r * 2, r * 2),
            style = Stroke(width = strokeW, cap = StrokeCap.Round)
        )
    }
}
