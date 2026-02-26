package uk.co.hushchip.app.ui.components.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import uk.co.hushchip.app.ui.theme.HushColors
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import uk.co.hushchip.app.R
@Composable
fun InfoPopUpDialog(
    isOpen: MutableState<Boolean>,
    title: Int,
    message: Int
) {
    if (!isOpen.value) return

    Dialog(
        onDismissRequest = {
            isOpen.value = !isOpen.value
        },
        properties = DialogProperties()
    ) {
        Column(
            modifier = Modifier
                .width(350.dp)
                .background(
                    color = HushColors.bgRaised,
                    shape = RoundedCornerShape(12.dp)
                )
                .clip(
                    shape = RoundedCornerShape(12.dp)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HushColors.bgSurface),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = stringResource(id = title),
                    fontSize = 14.sp,
                    color = HushColors.textBright,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = 3.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                text = stringResource(id = message),
                fontSize = 12.sp,
                color = HushColors.textMuted,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            HushButton(
                onClick = { isOpen.value = !isOpen.value },
                text = R.string.close
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}