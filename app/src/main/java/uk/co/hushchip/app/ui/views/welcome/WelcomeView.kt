package uk.co.hushchip.app.ui.views.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import uk.co.hushchip.app.R
import uk.co.hushchip.app.ui.components.shared.NextButton
import uk.co.hushchip.app.ui.components.shared.HushButton
import uk.co.hushchip.app.ui.components.shared.StepCircles
import uk.co.hushchip.app.ui.components.shared.WelcomeViewTitle
import uk.co.hushchip.app.ui.theme.HushButtonBlue
import uk.co.hushchip.app.ui.theme.HushButtonPurple
import uk.co.hushchip.app.ui.theme.HushColors

@Composable
fun WelcomeView(
    title: Int,
    text: Int,
    link: String? = null,
    colors: List<Color>? = null,
    backgroundImage: Int,
    isFullWidth: Boolean = false,
    onNext: () -> Unit,
    onBack: () -> Unit,
    onClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HushColors.bg)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { change, dragAmount ->
                    when {
                        dragAmount > 0 -> {
                            onBack()
                        }

                        dragAmount < 0 -> {
                            onNext()
                        }
                    }
                    change.consume()
                }
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = scrollState),
        ) {
            WelcomeViewTitle()
            Spacer(modifier = Modifier.height(16.dp))
            WelcomeViewContent(
                title = title,
                text = text,
                urlString = link,
                onClick = onClick
            )
            Spacer(modifier = Modifier.height(30.dp))
            Image(
                painter = painterResource(backgroundImage),
                contentDescription = null,
                alignment = Alignment.Center,
                modifier = if (isFullWidth) Modifier.fillMaxWidth() else Modifier
                    .size(300.dp),
                contentScale = if (isFullWidth) ContentScale.FillWidth else ContentScale.Fit
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .padding(bottom = 55.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    colors?.let {
                        NextButton(
                            onClick = onNext
                        )
                        StepCircles(colors)
                    } ?: run {
                        HushButton(
                            modifier = Modifier
                                .padding(
                                    horizontal = 20.dp
                                ),
                            onClick = onNext,
                            buttonColor = HushButtonBlue,
                            text = R.string.start
                        )
                    }
                }
            }
        }
    }
}
