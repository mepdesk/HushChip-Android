package uk.co.hushchip.app.ui.views.home

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import uk.co.hushchip.app.AddSecretView
import uk.co.hushchip.app.MySecretView
import uk.co.hushchip.app.PinEntryView
import uk.co.hushchip.app.data.NfcActionType
import uk.co.hushchip.app.data.NfcResultCode
import uk.co.hushchip.app.data.PinCodeAction
import uk.co.hushchip.app.ui.components.home.HomeHeaderRow
import uk.co.hushchip.app.ui.theme.HushColors
import uk.co.hushchip.app.ui.theme.outfitFamily
import uk.co.hushchip.app.viewmodels.SharedViewModel

@Composable
fun HomeView(
    context: Context,
    navController: NavHostController,
    viewModel: SharedViewModel,
) {
    // Auto-start NFC reader mode when home screen is visible (card not yet scanned)
    // Uses DO_NOTHING action so card detection doesn't execute any APDU commands
    LaunchedEffect(viewModel.isCardDataAvailable) {
        if (!viewModel.isCardDataAvailable) {
            viewModel.scanCardForAction(
                activity = context as Activity,
                nfcActionType = NfcActionType.DO_NOTHING
            )
        }
    }

    // When card is detected (isConnected flips to true), navigate to PIN entry
    LaunchedEffect(viewModel.isCardConnected) {
        if (viewModel.isCardConnected && !viewModel.isCardDataAvailable) {
            viewModel.setResultCodeLiveTo(NfcResultCode.NONE)
            navController.navigate(
                PinEntryView(
                    pinCodeAction = PinCodeAction.ENTER_PIN_CODE.name,
                    isBackupCard = false,
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HushColors.bg)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HomeHeaderRow(
                context = context,
                navController = navController,
                viewModel = viewModel,
            )
            if (viewModel.isCardDataAvailable) {
                SecretsList(
                    cardLabel = viewModel.cardLabel,
                    secretHeaders = viewModel.secretHeaders,
                    addNewSecret = {
                        navController.navigate(AddSecretView)
                    },
                    onSecretClick = { secretHeader ->
                        secretHeader.let {
                            viewModel.updateCurrentSecretHeader(secretHeader)
                            navController.navigate(MySecretView)
                        }
                    },
                )
            } else {
                // Pre-scan state: card illustration + tap instructions
                // NFC Reader Mode is silently active — waiting for card tap
                Spacer(modifier = Modifier.weight(1f))
                CardIllustration()
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Tap your card",
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 18.sp,
                        color = HushColors.textBright,
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Hold your HushChip near the\nback of your phone",
                    modifier = Modifier.widthIn(max = 280.dp),
                    style = TextStyle(
                        fontFamily = outfitFamily,
                        fontWeight = FontWeight.Light,
                        fontSize = 14.sp,
                        color = HushColors.textMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun CardIllustration(
    cardWidth: Int = 260,
    cardHeight: Int = 164,
) {
    val goldColor = Color(0xFFB8A04A)

    Box(
        modifier = Modifier
            .width(cardWidth.dp)
            .height(cardHeight.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.2f)
            )
            .background(
                color = HushColors.bgRaised,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.dp,
                color = HushColors.border,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // Gold EMV chip in top-left with contact pad detail lines
        Canvas(
            modifier = Modifier
                .padding(start = 24.dp, top = 24.dp)
                .size(width = 40.dp, height = 30.dp)
                .align(Alignment.TopStart)
        ) {
            drawRoundRect(
                color = goldColor,
                cornerRadius = CornerRadius(4f, 4f)
            )
            val lineColor = goldColor.copy(alpha = 0.4f)
            // Horizontal contact pad lines
            for (i in 1..3) {
                val y = size.height * i / 4f
                drawLine(
                    color = lineColor,
                    start = Offset(2f, y),
                    end = Offset(size.width - 2f, y),
                    strokeWidth = 1.2f
                )
            }
            // Vertical centre line
            drawLine(
                color = lineColor,
                start = Offset(size.width / 2f, 2f),
                end = Offset(size.width / 2f, size.height - 2f),
                strokeWidth = 1.2f
            )
        }

        // NFC/contactless symbol in top-right — 3 concentric arcs
        Canvas(
            modifier = Modifier
                .padding(end = 24.dp, top = 28.dp)
                .size(22.dp)
                .align(Alignment.TopEnd)
        ) {
            val arcColor = HushColors.textFaint.copy(alpha = 0.4f)
            val center = Offset(size.width * 0.3f, size.height * 0.7f)
            // Small dot at origin
            drawCircle(
                color = arcColor,
                radius = 1.5f,
                center = center
            )
            for (i in 1..3) {
                val radius = size.minDimension * 0.15f * i
                drawArc(
                    color = arcColor,
                    startAngle = -60f,
                    sweepAngle = 120f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 1.5f)
                )
            }
        }

        // "H U S H" text bottom-left
        Text(
            text = "H U S H",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 20.dp),
            style = TextStyle(
                fontFamily = outfitFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                letterSpacing = 4.sp,
                color = HushColors.textFaint
            )
        )
    }
}
