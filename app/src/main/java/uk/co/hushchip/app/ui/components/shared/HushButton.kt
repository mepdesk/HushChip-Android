package uk.co.hushchip.app.ui.components.shared

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.ui.theme.outfitFamily

@Composable
fun HushButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    text: Int,
    buttonColor: Color = HushColors.bgRaised,
    textColor: Color = HushColors.textMuted,
    image: Int? = null,
    horizontalPadding: Dp = 0.dp,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    Button(
        onClick = { onClick() },
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .height(52.dp)
            .shadow(
                elevation = 4.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.4f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.06f),
                        Color.White.copy(alpha = 0.02f),
                    )
                ),
                shape = shape
            ),
        shape = shape,
        colors = ButtonColors(
            contentColor = textColor,
            containerColor = buttonColor,
            disabledContainerColor = buttonColor.copy(alpha = 0.4f),
            disabledContentColor = textColor.copy(alpha = 0.4f)
        )
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(text).uppercase(),
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    letterSpacing = 3.sp,
                    color = textColor
                )
            )
            image?.let {
                Spacer(modifier = Modifier.width(8.dp))
                GifImage(
                    modifier = Modifier.size(16.dp),
                    colorFilter = ColorFilter.tint(textColor),
                    image = image
                )
            }
        }
    }
}
