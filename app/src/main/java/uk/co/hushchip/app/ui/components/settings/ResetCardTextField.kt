package uk.co.hushchip.app.ui.components.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import uk.co.hushchip.app.ui.theme.HushColors
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.hushchip.app.R

@Composable
fun ResetCardTextField(
    title: Int = R.string.factoryReset,
    text: Int,
    warning: Int? = null,
    subText: Int? = null,
    subTextDescription: Int? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 20.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(id = title),
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = HushColors.textBright,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(id = text),
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = HushColors.textBody,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraLight
            )
        )
        Spacer(modifier = Modifier.height(12.dp))

        warning?.let {
            Text(
                text = stringResource(id = warning),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    color = HushColors.textBody,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        subText?.let {
            Text(
                text = stringResource(id = subText),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    color = HushColors.textBody,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraLight
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            subTextDescription?.let {
                Text(
                    text = stringResource(id = subTextDescription),
                    textAlign = TextAlign.Center,
                    style = TextStyle(
                        color = HushColors.textBody,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraLight,
                        textAlign = TextAlign.Center
                    )
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
    Spacer(modifier = Modifier.height(32.dp))
}