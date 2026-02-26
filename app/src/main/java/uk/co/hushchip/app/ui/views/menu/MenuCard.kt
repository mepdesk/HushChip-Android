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

package uk.co.hushchip.app.ui.views.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import uk.co.hushchip.app.ui.theme.HushColors
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MenuCard(
    modifier: Modifier,
    text: String,
    textMessage: String?  = null,
    textAlign: Alignment,
    color: Color,
    drawableId: Int? = null,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .background(
                color = color,
                shape = RoundedCornerShape(15.dp)
            )
            .clickable { onClick() },
    ) {
        val endTextPadding = if (drawableId == null) 15.dp else 30.dp //40.dp
        Column(
            modifier = Modifier
                .align(textAlign)
                .padding(top = 20.dp, start = 15.dp, bottom = 15.dp, end = endTextPadding),
        ) {
            Text(
                modifier = Modifier,
                color = HushColors.textBright,
                fontSize = 16.sp,
                text = text,
                fontWeight = FontWeight.ExtraBold,
            )
            textMessage?.let {
                Text(
                    modifier = Modifier,
                    color = HushColors.textBright,
                    fontSize = 14.sp,
                    text = textMessage,
                    fontWeight =  FontWeight.ExtraLight
                )
            }
        }
        if (drawableId != null) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Image(
                    modifier = Modifier
                        .size(30.dp),
                    painter = painterResource(id = drawableId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(HushColors.textBright)
                )
            }
        }
    }
}