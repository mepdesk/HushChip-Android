package uk.co.signstr.app.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (identity in identities) {
            val isActive = identity.id == activeIdentity?.id
            val initials = getInitials(identity.name)

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .signstrClickEffect(onClick = { onSelect(identity) })
                    .background(
                        if (isActive) SignstrColors.bgRaised else SignstrColors.bg,
                        CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = if (isActive) SignstrColors.textMuted else SignstrColors.border,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = if (isActive) SignstrColors.textBright else SignstrColors.textFaint,
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}

private fun getInitials(name: String): String {
    val trimmed = name.trim()
    if (trimmed.isEmpty()) return "?"
    val parts = trimmed.split(" ").filter { it.isNotEmpty() }
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
        trimmed.length >= 2 -> trimmed.take(2).uppercase()
        else -> trimmed.first().uppercase().toString()
    }
}
