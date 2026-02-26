package uk.co.hushchip.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val HushDarkScheme = darkColorScheme(
    primary = HushColors.textMuted,
    onPrimary = HushColors.textWhite,
    primaryContainer = HushColors.bgRaised,
    onPrimaryContainer = HushColors.textBody,
    secondary = HushColors.textFaint,
    onSecondary = HushColors.textWhite,
    secondaryContainer = HushColors.bgSurface,
    onSecondaryContainer = HushColors.textBody,
    tertiary = HushColors.textFaint,
    onTertiary = HushColors.textWhite,
    background = HushColors.bg,
    onBackground = HushColors.textBody,
    surface = HushColors.bg,
    onSurface = HushColors.textBody,
    surfaceVariant = HushColors.bgRaised,
    onSurfaceVariant = HushColors.textMuted,
    outline = HushColors.border,
    outlineVariant = HushColors.borderHover,
    error = HushColors.danger,
    onError = HushColors.textWhite,
)

@Composable
fun HushChipTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = HushColors.bg.toArgb()
            window.navigationBarColor = HushColors.bg.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }
    MaterialTheme(colorScheme = HushDarkScheme, typography = Typography, content = content)
}
