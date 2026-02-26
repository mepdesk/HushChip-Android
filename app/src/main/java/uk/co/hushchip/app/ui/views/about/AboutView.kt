// Copyright (c) 2026 Gridmark Technologies Ltd (HushChip)
// https://github.com/hushchip/HushChip-Android
//
// Based on Seedkeeper-Android by Toporin / Satochip S.R.L.
// https://github.com/Toporin/Seedkeeper-Android
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.

package uk.co.hushchip.app.ui.views.about

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import uk.co.hushchip.app.BuildConfig
import uk.co.hushchip.app.R
import uk.co.hushchip.app.ui.components.shared.HeaderAlternateRow
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.ui.theme.outfitFamily
import uk.co.hushchip.app.utils.hushClickEffect

@Composable
fun AboutView(
    context: Context,
    navController: NavHostController,
) {
    val scrollState = rememberScrollState()

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
                titleText = "About"
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                // Card-mark icon
                Image(
                    painter = painterResource(id = R.drawable.ic_hush_cardmark),
                    contentDescription = "HushChip",
                    modifier = Modifier.size(48.dp),
                    colorFilter = ColorFilter.tint(HushColors.textFaint)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // HUSHCHIP wordmark
                Text(
                    text = "H U S H C H I P",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        letterSpacing = 4.sp,
                        color = HushColors.textBright
                    )
                )
                Spacer(modifier = Modifier.height(6.dp))

                // Version string
                Text(
                    text = "Version ${BuildConfig.VERSION_NAME}",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = HushColors.textFaint
                    )
                )

                Spacer(modifier = Modifier.height(32.dp))

                // OPEN SOURCE section
                SectionHeader(text = "OPEN SOURCE")
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "HushChip is open-source software licensed under the GNU Affero General Public License v3.0 (AGPLv3).\n\nYou are free to use, modify, and distribute this software under the terms of the licence.",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = HushColors.textMuted,
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                LinkGroup {
                    LinkRow(
                        title = "VIEW SOURCE CODE",
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/hushchip/HushChip-Android"))
                            )
                        }
                    )
                    GroupDivider()
                    LinkRow(
                        title = "VIEW LICENCE",
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/hushchip/HushChip-Android/blob/main/LICENSE"))
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ATTRIBUTION section
                SectionHeader(text = "ATTRIBUTION")
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Based on Seedkeeper-Android by Toporin / Satochip S.R.L.\n\nCard communication powered by the Satochip library.",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = HushColors.textMuted,
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                LinkGroup {
                    LinkRow(
                        title = "ORIGINAL PROJECT",
                        onClick = {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Toporin/Seedkeeper-Android"))
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // LEGAL section
                SectionHeader(text = "LEGAL")
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "HushChip is a trading name of Gridmark Technologies Ltd\nCompany No. XXXXXXXX\n\nhushchip.co.uk",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 12.sp,
                        color = HushColors.textMuted,
                        lineHeight = 18.sp
                    )
                )

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
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
                color = HushColors.textFaint
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

@Composable
private fun LinkGroup(content: @Composable () -> Unit) {
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
        content()
    }
}

@Composable
private fun GroupDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 14.dp),
        thickness = 1.dp,
        color = HushColors.border
    )
}

@Composable
private fun LinkRow(
    title: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .hushClickEffect(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = HushColors.textBody
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
