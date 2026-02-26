package uk.co.hushchip.app.ui.components.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import uk.co.hushchip.app.R
import uk.co.hushchip.app.ui.theme.HushColors

@Composable
fun WelcomeViewTitle(
    modifier: Modifier = Modifier
        .padding(horizontal = 20.dp)
        .width(60.dp)
        .height(44.dp)
) {
    Image(
        painter = painterResource(R.drawable.ic_hush_cardmark),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit,
        colorFilter = ColorFilter.tint(HushColors.textFaint)
    )
}
