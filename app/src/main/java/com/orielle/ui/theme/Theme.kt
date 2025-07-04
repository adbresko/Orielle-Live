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

// Define a new, darker blue for high-contrast primary actions
private val DeepWaterBlue = Color(0xFF006599)

private val DarkColorScheme = darkColorScheme(
    primary = WaterBlue, // Keep the lighter blue as the primary accent in dark mode
    onPrimary = Charcoal,
    secondary = AuroraGold,
    onSecondary = OnAuroraGold,
    background = DarkGray,
    onBackground = White,
    surface = DarkGray,
    onSurface = White,
    error = ErrorRed,
    onError = White
)

private val LightColorScheme = lightColorScheme(
    primary = DeepWaterBlue, // Use the high-contrast blue for primary actions (buttons, links)
    onPrimary = White,       // Text on top of DeepWaterBlue should be white
    primaryContainer = WaterBlue, // Use the original, lighter blue for container backgrounds
    onPrimaryContainer = Charcoal,
    secondary = AuroraGold,
    onSecondary = OnAuroraGold,
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
            // Use the background color for the status bar for a more seamless look
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = OrielleShapes, // Integrate custom shapes
        content = content
    )
}
