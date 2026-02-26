package uk.co.hushchip.app.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.ui.theme.outfitFamily
import uk.co.hushchip.app.utils.hushClickEffect

@Composable
fun HeaderAlternateRow(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    titleText: String? = null,
    message: Int? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button - pill shape
        Box(
            modifier = Modifier
                .height(36.dp)
                .hushClickEffect(onClick = { onClick() })
                .background(
                    color = HushColors.bgRaised,
                    shape = RoundedCornerShape(20.dp)
                )
                .border(
                    width = 1.dp,
                    color = HushColors.border,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "\u2039 Back",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = HushColors.textMuted
                )
            )
        }

        // Title
        titleText?.let {
            if (it.isNotEmpty()) {
                Text(
                    textAlign = TextAlign.Center,
                    text = it.uppercase(),
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        letterSpacing = 4.sp,
                        color = HushColors.textFaint
                    )
                )
            }
        }

        // Spacer to balance layout
        Spacer(modifier = Modifier.width(60.dp))
    }
    message?.let {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            textAlign = TextAlign.Center,
            text = stringResource(message),
            modifier = Modifier.padding(horizontal = 20.dp),
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Light,
                fontSize = 14.sp,
                color = HushColors.textMuted,
                lineHeight = 20.sp
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
