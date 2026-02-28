package uk.co.signstr.app.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.signstr.app.ui.theme.SignstrColors
import uk.co.signstr.app.ui.theme.outfitFamily
import uk.co.signstr.app.utils.signstrClickEffect

@Composable
fun GhostButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDanger: Boolean = false,
    enabled: Boolean = true
) {
    val bgColor = if (isDanger) Color.Transparent else SignstrColors.border
    val borderColor = if (isDanger) SignstrColors.dangerBorder else SignstrColors.borderHover
    val textColor = when {
        !enabled -> SignstrColors.textGhost
        isDanger -> SignstrColors.danger
        else -> SignstrColors.textBright
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.signstrClickEffect(onClick = onClick) else Modifier)
            .background(bgColor, RoundedCornerShape(10.dp))
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                letterSpacing = 4.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )
        )
    }
}
