package com.orielle.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = StillwaterTeal, // Use Stillwater Teal for primary actions in dark mode
    onPrimary = White,
    secondary = AuroraGold,
    onSecondary = Charcoal,
    background = DarkGray,
    onBackground = White,
    surface = DarkGray,
    onSurface = White,
    error = ErrorRed,
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = StillwaterTeal,      // Use Stillwater Teal for high-contrast primary actions
    onPrimary = White,            // Text on top of Stillwater Teal should be white
    primaryContainer = WaterBlue, // Use the original, lighter blue for container backgrounds
    onPrimaryContainer = Charcoal,
    secondary = AuroraGold,
    onSecondary = Charcoal,
    background = SoftSand,
    onBackground = Charcoal,
    surface = White,
    onSurface = Charcoal,
    error = ErrorRed,
    onError = White
)

@Composable
fun OrielleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = OrielleShapes,
        content = content
    )
}
