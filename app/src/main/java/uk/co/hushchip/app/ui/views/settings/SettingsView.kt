package uk.co.hushchip.app.ui.views.settings

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import uk.co.hushchip.app.FactoryResetView
import uk.co.hushchip.app.R
import uk.co.hushchip.app.ShowLogsView
import uk.co.hushchip.app.data.HushChipPreferences
import uk.co.hushchip.app.ui.components.shared.HeaderAlternateRow
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.ui.theme.outfitFamily
import uk.co.hushchip.app.utils.hushClickEffect
import uk.co.hushchip.app.viewmodels.SharedViewModel

@Composable
fun SettingsView(
    context: Context,
    navController: NavHostController,
    viewModel: SharedViewModel,
) {
    val scrollState = rememberScrollState()
    val settings = context.getSharedPreferences("seedkeeper", Context.MODE_PRIVATE)
    val starterIntro = remember {
        mutableStateOf(settings.getBoolean(HushChipPreferences.FIRST_TIME_LAUNCH.name, true))
    }
    val debugMode = remember {
        mutableStateOf(settings.getBoolean(HushChipPreferences.DEBUG_MODE.name, false))
    }
    val logsDisabledText = stringResource(id = R.string.logsDisabledText)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HushColors.bg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = scrollState)
        ) {
            HeaderAlternateRow(
                onClick = { navController.popBackStack() },
                titleText = stringResource(R.string.settings)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                // APP section
                SectionLabel("APP")
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 2.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = Color.Black.copy(alpha = 0.3f),
                            spotColor = Color.Black.copy(alpha = 0.2f)
                        )
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    HushColors.bgRaised,
                                    Color(0xFF0C0C0E),
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .drawBehind {
                            drawLine(
                                color = Color.White.copy(alpha = 0.04f),
                                start = Offset(8.dp.toPx(), 0f),
                                end = Offset(size.width - 8.dp.toPx(), 0f),
                                strokeWidth = 1f
                            )
                        }
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.06f),
                                    Color.White.copy(alpha = 0.02f),
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Show onboarding",
                            modifier = Modifier.weight(1f),
                            style = TextStyle(
                                fontFamily = outfitFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = HushColors.textBody
                            )
                        )
                        Switch(
                            checked = starterIntro.value,
                            onCheckedChange = {
                                starterIntro.value = it
                                settings.edit().putBoolean(
                                    HushChipPreferences.FIRST_TIME_LAUNCH.name,
                                    it
                                ).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = HushColors.textBright,
                                checkedTrackColor = HushColors.borderHover,
                                uncheckedThumbColor = HushColors.textFaint,
                                uncheckedTrackColor = HushColors.bgSurface,
                                uncheckedBorderColor = HushColors.border
                            )
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        thickness = 1.dp,
                        color = HushColors.border
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Debug mode",
                            modifier = Modifier.weight(1f),
                            style = TextStyle(
                                fontFamily = outfitFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = HushColors.textBody
                            )
                        )
                        Switch(
                            checked = debugMode.value,
                            onCheckedChange = {
                                debugMode.value = it
                                settings.edit().putBoolean(
                                    HushChipPreferences.DEBUG_MODE.name,
                                    it
                                ).apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = HushColors.textBright,
                                checkedTrackColor = HushColors.borderHover,
                                uncheckedThumbColor = HushColors.textFaint,
                                uncheckedTrackColor = HushColors.bgSurface,
                                uncheckedBorderColor = HushColors.border
                            )
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 14.dp),
                        thickness = 1.dp,
                        color = HushColors.border
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .hushClickEffect(onClick = {
                                if (debugMode.value) {
                                    navController.navigate(ShowLogsView)
                                } else {
                                    Toast.makeText(context, logsDisabledText, Toast.LENGTH_SHORT).show()
                                }
                            })
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Show logs",
                            modifier = Modifier.weight(1f),
                            style = TextStyle(
                                fontFamily = outfitFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (debugMode.value) HushColors.textBody else HushColors.textFaint
                            )
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_chevron_right),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            colorFilter = ColorFilter.tint(HushColors.textFaint)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // DANGER ZONE
                SectionLabel("DANGER ZONE", HushColors.danger)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(12.dp),
                            ambientColor = HushColors.danger.copy(alpha = 0.15f),
                            spotColor = HushColors.danger.copy(alpha = 0.1f)
                        )
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    HushColors.bgRaised,
                                    Color(0xFF0C0C0E),
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = HushColors.danger.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .hushClickEffect(onClick = {
                            navController.navigate(FactoryResetView)
                        })
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_warning_outline),
                            contentDescription = "Warning",
                            modifier = Modifier.size(20.dp),
                            colorFilter = ColorFilter.tint(HushColors.danger)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Factory Reset",
                            modifier = Modifier.weight(1f),
                            style = TextStyle(
                                fontFamily = outfitFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 14.sp,
                                color = HushColors.danger
                            )
                        )
                        Image(
                            painter = painterResource(id = R.drawable.ic_chevron_right),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            colorFilter = ColorFilter.tint(HushColors.danger)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, color: androidx.compose.ui.graphics.Color = HushColors.textFaint) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                color = color
            )
        )
        Spacer(modifier = Modifier.width(12.dp))
        HorizontalDivider(
            color = HushColors.border.copy(alpha = 0.5f),
            modifier = Modifier.weight(1f),
            thickness = 0.5.dp
        )
    }
}
