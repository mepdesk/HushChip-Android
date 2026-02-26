package uk.co.hushchip.app.ui.components.shared

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import uk.co.hushchip.app.ui.theme.HushColors
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import uk.co.hushchip.app.R
import uk.co.hushchip.app.utils.hushClickEffect

@Composable
fun InputPinField(
    modifier: Modifier = Modifier,
    isEditable: Boolean = true,
    curValue: MutableState<String>,
    placeHolder: Int? = null,
    visualTransformation: VisualTransformation = PasswordVisualTransformation(),
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    val passwordVisibility = remember {
        mutableStateOf(visualTransformation != PasswordVisualTransformation())
    }

    val trailingIcon: (@Composable () -> Unit)? = @Composable {
        Image(
            modifier = Modifier
                .size(24.dp)
                .hushClickEffect(
                    onClick = {
                        passwordVisibility.value = !passwordVisibility.value
                    }
                ),
            painter = painterResource(id = if (passwordVisibility.value) R.drawable.show_password else R.drawable.hide_password),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(HushColors.textMuted)
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            modifier = modifier
                .fillMaxWidth()
                .border(1.dp, HushColors.border, shape = RoundedCornerShape(8.dp))
                .clip(
                    RoundedCornerShape(8.dp)
                )
                .focusRequester(focusRequester)
                .onFocusChanged {
                    if (it.isFocused) {
                        keyboardController?.show()
                    }
                },
            enabled = isEditable,
            value = curValue.value,
            onValueChange = {
                curValue.value = it
            },
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                autoCorrect = false,
                keyboardType = KeyboardType.Text
            ),
            visualTransformation = if (passwordVisibility.value) VisualTransformation.None else visualTransformation,
            textStyle = TextStyle(
                color = HushColors.textBright,
                fontSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
            ),
            placeholder = {
                placeHolder?.let {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(id = placeHolder),
                        style = TextStyle(
                            color = HushColors.textGhost,
                            fontSize = 18.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.ExtraLight
                        )
                    )
                }
            },
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                focusedTextColor = HushColors.textBright,
                unfocusedTextColor = HushColors.textBright,
                cursorColor = HushColors.textBright,
                focusedLabelColor = HushColors.textBright,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                disabledTextColor = HushColors.textBright,
            ),
            minLines = 1,
            maxLines = 1,
            trailingIcon = trailingIcon
        )
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}