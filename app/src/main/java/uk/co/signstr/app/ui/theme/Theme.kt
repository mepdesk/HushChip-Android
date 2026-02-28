package uk.co.signstr.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val SignstrDarkScheme = darkColorScheme(
    primary = SignstrColors.textMuted,
    onPrimary = SignstrColors.textWhite,
    primaryContainer = SignstrColors.bgRaised,
    onPrimaryContainer = SignstrColors.textBody,
    secondary = SignstrColors.textFaint,
    onSecondary = SignstrColors.textWhite,
    secondaryContainer = SignstrColors.bgSurface,
    onSecondaryContainer = SignstrColors.textBody,
    tertiary = SignstrColors.textFaint,
    onTertiary = SignstrColors.textWhite,
    background = SignstrColors.bg,
    onBackground = SignstrColors.textBody,
    surface = SignstrColors.bg,
    onSurface = SignstrColors.textBody,
    surfaceVariant = SignstrColors.bgRaised,
    onSurfaceVariant = SignstrColors.textMuted,
    outline = SignstrColors.border,
    outlineVariant = SignstrColors.borderHover,
    error = SignstrColors.danger,
    onError = SignstrColors.textWhite,
)

@Composable
fun SignstrTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = SignstrColors.bg.toArgb()
            window.navigationBarColor = SignstrColors.bg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }
    MaterialTheme(colorScheme = SignstrDarkScheme, typography = SignstrTypography, content = content)
}
