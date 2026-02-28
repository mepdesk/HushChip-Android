package uk.co.signstr.app.ui.views.onboarding

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SignstrColors.bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.3f))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(0.5f)
        ) { page ->
            val p = pages[page]
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
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
