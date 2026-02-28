package uk.co.signstr.app.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.signstr.app.data.SignstrIdentity
import uk.co.signstr.app.ui.theme.SignstrColors
import uk.co.signstr.app.ui.theme.outfitFamily
import uk.co.signstr.app.utils.signstrClickEffect

@Composable
fun IdentityPicker(
    identities: List<SignstrIdentity>,
    activeIdentity: SignstrIdentity?,
    onSelect: (SignstrIdentity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (identities.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        for (identity in identities) {
            val isActive = identity.id == activeIdentity?.id
            Box(
                modifier = Modifier
                    .signstrClickEffect(onClick = { onSelect(identity) })
                    .background(
                        if (isActive) SignstrColors.borderHover else SignstrColors.bgRaised,
                        RoundedCornerShape(20.dp)
                    )
                    .border(
                        1.dp,
                        if (isActive) SignstrColors.textFaint else SignstrColors.border,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = identity.name.ifEmpty { identity.pubkeyHex.take(8) + "..." },
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = if (isActive) SignstrColors.textBright else SignstrColors.textMuted
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
