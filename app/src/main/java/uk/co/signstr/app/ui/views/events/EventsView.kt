package uk.co.signstr.app.ui.views.events

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.signstr.app.data.EventLogEntry
import uk.co.signstr.app.ui.components.shared.IdentityPicker
import uk.co.signstr.app.ui.theme.SignstrColors
import uk.co.signstr.app.ui.theme.outfitFamily
import uk.co.signstr.app.viewmodels.SignstrViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EventsView(viewModel: SignstrViewModel) {
    val events = viewModel.getActiveEventLog()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SignstrColors.bg)
    ) {
        IdentityPicker(
            identities = viewModel.identities,
            activeIdentity = viewModel.activeIdentity.value,
            onSelect = { viewModel.setActiveIdentity(it) }
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp)
        ) {
            Text(
                text = "EVENT LOG",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp,
                    color = SignstrColors.textFaint
                )
            )
            Spacer(modifier = Modifier.width(12.dp))
            HorizontalDivider(
                color = SignstrColors.border.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f),
                thickness = 0.5.dp
            )
        }

        if (events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No signing events yet.\nEvents will appear here as\nconnected apps make requests.",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 13.sp,
                        color = SignstrColors.textFaint,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(events) { entry ->
                    EventLogCard(entry)
                }
            }
        }
    }
}

@Composable
private fun EventLogCard(entry: EventLogEntry) {
    val dateFormat = SimpleDateFormat("MMM d, HH:mm:ss", Locale.getDefault())
    val timeStr = dateFormat.format(Date(entry.timestamp))

    val badgeColor = when (entry.statusBadge) {
        "REJECTED" -> SignstrColors.danger
        "SAFE-AUTO" -> SignstrColors.textGhost
        "AUTO-APPROVED" -> SignstrColors.textFaint
        else -> SignstrColors.success
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SignstrColors.bgRaised, RoundedCornerShape(10.dp))
            .border(1.dp, SignstrColors.border, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.kindDescription,
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = SignstrColors.textBody
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row {
                    Text(
                        text = entry.clientName,
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 11.sp,
                            color = SignstrColors.textFaint
                        )
                    )
                    entry.eventKind?.let { kind ->
                        Text(
                            text = "  kind $kind",
                            style = TextStyle(
                                fontFamily = outfitFamily,
                                fontWeight = FontWeight.Light,
                                fontSize = 11.sp,
                                color = SignstrColors.textGhost
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = timeStr,
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 10.sp,
                        color = SignstrColors.textGhost
                    )
                )
            }
            Text(
                text = entry.statusBadge,
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 9.sp,
                    letterSpacing = 1.sp,
                    color = badgeColor
                )
            )
        }
    }
}
