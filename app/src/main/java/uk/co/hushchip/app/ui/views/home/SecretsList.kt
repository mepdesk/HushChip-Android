package uk.co.hushchip.app.ui.views.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.satochip.client.seedkeeper.SeedkeeperSecretHeader
import uk.co.hushchip.app.ui.components.home.SecretButton
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.ui.theme.outfitFamily

@Composable
fun SecretsList(
    cardLabel: String,
    secretHeaders: SnapshotStateList<SeedkeeperSecretHeader?>,
    addNewSecret: () -> Unit,
    onSecretClick: (SeedkeeperSecretHeader) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val filteredList = if (searchQuery.isEmpty()) {
        secretHeaders.toList()
    } else {
        secretHeaders.toList().filter {
            it?.label?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    val secretCount = secretHeaders.filterNotNull().size
    val memoryPercent = if (secretCount > 0) (secretCount * 8).coerceAtMost(100) else 0

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            // Memory usage bar
            LinearProgressIndicator(
                progress = { memoryPercent / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp)),
                color = HushColors.textFaint,
                trackColor = HushColors.border,
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Stats text
            Text(
                text = "$secretCount SECRETS \u00B7 ${memoryPercent}% MEMORY USED",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp,
                    letterSpacing = 3.sp,
                    color = HushColors.textFaint,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Search field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .background(
                        color = HushColors.bgRaised,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = HushColors.border,
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = "Search",
                        modifier = Modifier.size(18.dp),
                        tint = HushColors.textFaint
                    )
                    Spacer(modifier = Modifier.padding(start = 8.dp))
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = HushColors.textBody
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                        decorationBox = { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search secrets",
                                    style = TextStyle(
                                        fontFamily = outfitFamily,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 14.sp,
                                        color = HushColors.textFaint
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            // Secret list or empty state
            if (filteredList.isEmpty() && searchQuery.isEmpty()) {
                Spacer(modifier = Modifier.weight(1f))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No secrets stored",
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 15.sp,
                            color = HushColors.textMuted
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap + to add your first secret",
                        style = TextStyle(
                            fontFamily = outfitFamily,
                            fontWeight = FontWeight.Light,
                            fontSize = 12.sp,
                            color = HushColors.textFaint
                        )
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(state = scrollState)
                        .padding(bottom = 80.dp)
                ) {
                    filteredList.forEach { secret ->
                        SecretButton(
                            secretHeader = secret,
                            onClick = {
                                secret?.let { onSecretClick(secret) }
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = { addNewSecret() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .size(56.dp)
                .border(
                    width = 1.dp,
                    color = HushColors.border,
                    shape = CircleShape
                ),
            containerColor = HushColors.bg,
            elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp),
            shape = CircleShape
        ) {
            Text(
                text = "+",
                style = TextStyle(
                    fontFamily = outfitFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 24.sp,
                    color = HushColors.textBody
                )
            )
        }
    }
}
