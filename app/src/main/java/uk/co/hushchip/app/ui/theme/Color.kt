package uk.co.hushchip.app.ui.theme

import androidx.compose.ui.graphics.Color

object HushColors {
    val bg            = Color(0xFF09090B)
    val bgRaised      = Color(0xFF0E0E10)
    val bgSurface     = Color(0xFF111113)
    val border        = Color(0xFF1A1A1E)
    val borderHover   = Color(0xFF28282E)
    val textGhost     = Color(0xFF38383E)
    val textFaint     = Color(0xFF5A5A64)
    val textMuted     = Color(0xFF8A8A96)
    val textBody      = Color(0xFFA8A8B4)
    val textBright    = Color(0xFFCDCDD6)
    val textWhite     = Color(0xFFE4E4EC)
    val danger        = Color(0xFFC45555)
    val dangerBorder  = Color(0xFF3D2020)
    val dangerBg      = Color(0x26C45555)
}

// Keep old names as aliases temporarily so the build doesn't break.
// These will be removed once all components are updated.
val HushButtonBlue = HushColors.borderHover
val HushButtonPurple = HushColors.bg
val SatoButtonBlue = HushColors.borderHover
val SatoGradientPurple = HushColors.bgRaised
val SatoButtonPurple = HushColors.bg
val SatoGradientPurpleLight = HushColors.textFaint
val SatoGray = HushColors.textBody
val SatoLightGrey = HushColors.textMuted
val SatoDarkPurple = HushColors.bg
val SatoLightPurple = HushColors.borderHover
val SatoDividerPurple = HushColors.border
val SatoCardPurple = HushColors.bgRaised
val SatoToggleGray = HushColors.textMuted
val SatoToggleBlack = HushColors.bg
val SatoGreen = HushColors.textMuted
val SatoPurple = HushColors.bg
val SatoDarkGray = HushColors.textFaint
val SatoActiveTracer = HushColors.textMuted
val SatoInactiveTracer = HushColors.textFaint
val SatoToggled = HushColors.borderHover
val SatoChecked = HushColors.textFaint
val SatoChecker = HushColors.textMuted
val SatoNfcBlue = HushColors.textMuted

// Standard Material colors (unused but keep for compilation)
val Purple80 = HushColors.textFaint
val PurpleGrey80 = HushColors.textFaint
val Pink80 = HushColors.textFaint
val Purple40 = HushColors.textFaint
val PurpleGrey40 = HushColors.textFaint
val Pink40 = HushColors.textFaint
