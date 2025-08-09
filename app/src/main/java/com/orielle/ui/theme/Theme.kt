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
    primary = StillwaterTeal,
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
    primary = StillwaterTeal,      // For filled buttons and major interactive elements
    onPrimary = White,            // Text on top of StillwaterTeal buttons
    primaryContainer = WaterBlue,
    onPrimaryContainer = Charcoal,
    secondary = AuroraGold,
    onSecondary = Charcoal,
    background = SoftSand,
    onBackground = Charcoal,      // Default text color on background is high-contrast
    surface = White,
    onSurface = Charcoal,         // Default text color on cards/surfaces is high-contrast
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
            // Make status bar transparent for edge-to-edge
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            // Set status bar icons to dark or light based on theme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = OrielleShapes,
        content = content
    )
}
