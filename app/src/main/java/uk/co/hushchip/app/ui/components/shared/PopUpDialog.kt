package uk.co.hushchip.app.ui.components.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import uk.co.hushchip.app.ui.theme.HushColors
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import uk.co.hushchip.app.R
import uk.co.hushchip.app.ui.components.import.InputField
import uk.co.hushchip.app.utils.hushClickEffect

@Composable
fun PopUpDialog(
    isOpen: MutableState<Boolean>,
    curValueLogin: MutableState<String>,
    title: Int,
    list: List<String>,
    onClick: (String) -> Unit
) {
    if (!isOpen.value) return
    var filteredList by remember {
        mutableStateOf(list)
    }
    val coroutineScope = rememberCoroutineScope()
    val searchQueryState = rememberUpdatedState(curValueLogin.value)

    Dialog(
        onDismissRequest = {
            isOpen.value = !isOpen.value
        },
        properties = DialogProperties()
    ) {
        Column (
            modifier = Modifier
                .width(350.dp)
                .height(350.dp)
                .background(
                    color = HushColors.bgRaised,
                    shape = RoundedCornerShape(8.dp)
                )
                .clip(
                    shape = RoundedCornerShape(8.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        HushColors.bgSurface
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(id = title),
                    fontSize = 18.sp,
                    color = HushColors.textWhite,
                    fontWeight = FontWeight.Bold,
                )
                Image(
                    modifier = Modifier
                        .background(Color.Transparent, shape = CircleShape)
                        .hushClickEffect(
                            onClick = {
                                isOpen.value = !isOpen.value
                            }
                        )
                        .padding(16.dp)
                        .width(24.dp),
                    painter = painterResource(R.drawable.cancel),
                    contentDescription =  null,
                    colorFilter = ColorFilter.tint(HushColors.textWhite),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                modifier = Modifier
                    .padding(horizontal = 8.dp),
                text = stringResource(id = R.string.emailListWriteNewOne),
                fontSize = 14.sp,
                color = HushColors.textBody,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(16.dp))

            InputField(
                modifier = Modifier.padding(horizontal = 8.dp),
                curValue = curValueLogin,
                placeHolder = R.string.loginOptional,
                containerColor = HushColors.bgSurface,
                onValueChange = {
                    coroutineScope.launch {
                        val searchQueryFlow = MutableStateFlow(searchQueryState.value)
                        searchQueryFlow
                            .debounce(500)
                            .distinctUntilChanged()
                            .collect { query ->
                                filteredList = if (curValueLogin.value.isEmpty()) {
                                    list
                                } else {
                                    list.filter { it.contains(curValueLogin.value, ignoreCase = true) }
                                }
                            }
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                modifier = Modifier
                    .padding(horizontal = 8.dp),
                text = stringResource(id = R.string.emailListUseExisting),
                fontSize = 14.sp,
                color = HushColors.textBody,
                fontWeight = FontWeight.Medium,
            )
            Spacer(modifier = Modifier.height(16.dp))

            Spacer(
                modifier = Modifier
                    .height(1.dp)
                    .fillMaxWidth()
                    .background(color = HushColors.border)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = HushColors.bgRaised),
                horizontalAlignment = Alignment.Start
            ) {
                filteredList.forEach { email ->
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    curValueLogin.value = email
                                    isOpen.value = false
                                }
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier,
                                text = email,
                                fontSize = 16.sp,
                                color = HushColors.textBody,
                                fontWeight = FontWeight.Bold,
                            )

                            Image(
                                modifier = Modifier
                                    .background(Color.Transparent, shape = CircleShape)
                                    .hushClickEffect(
                                        onClick = {
                                            onClick(email)
                                            filteredList = filteredList.toMutableList().apply {
                                                remove(email)
                                            }
                                        }
                                    )
                                    .padding(
                                        start = 12.dp,
                                        end = 16.dp,
                                        top = 16.dp,
                                        bottom = 16.dp
                                    )
                                    .width(16.dp),
                                painter = painterResource(R.drawable.cancel),
                                contentDescription =  null,
                                colorFilter = ColorFilter.tint(HushColors.textMuted),
                            )
                        }
                        Spacer(
                            modifier = Modifier
                                .height(1.dp)
                                .fillMaxWidth()
                                .background(color = HushColors.border)
                        )
                    }
                }
            }
        }
    }
}