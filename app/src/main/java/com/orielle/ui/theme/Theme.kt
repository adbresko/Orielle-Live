package com.orielle.ui.theme

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import android.os.Build

private val DarkColorScheme = darkColorScheme(
    primary = StillwaterTeal,
    onPrimary = White,
    primaryContainer = WaterBlue.copy(alpha = 0.2f), // Subtle blue tint
    onPrimaryContainer = White,
    secondary = AuroraGold,
    onSecondary = DarkGray,
    background = DarkGray,
    onBackground = White,
    surface = DarkSurface, // Distinct from background
    onSurface = White,
    surfaceVariant = MediumGray.copy(alpha = 0.3f), // Better contrast for tags/chips
    onSurfaceVariant = White.copy(alpha = 0.9f), // Better text contrast
    outline = MediumGray, // For borders in dark mode
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
    surfaceVariant = Color(0xFFEEEEEE), // Light gray for tags/chips
    onSurfaceVariant = Charcoal, // Good contrast for text on light gray
    outline = LightGray, // For borders in light mode
    error = ErrorRed,
    onError = White
)

@Composable
fun OrielleTheme(
    darkTheme: Boolean = false, // Default to light theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val responsiveTypography = getResponsiveTypography()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Make status bar and navigation bar transparent for edge-to-edge (API 21+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()

                // Set status bar icons to dark or light based on theme
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = responsiveTypography,
        shapes = OrielleShapes,
        content = content
    )
}

// Utility function to get card border based on theme
@Composable
fun getCardBorder(): BorderStroke? {
    return BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
}

// Utility function to get current theme colors
@Composable
fun getThemeColors() = MaterialTheme.colorScheme

/**
 * Composable that automatically observes the theme preference and applies it
 * This should be used in MainActivity and other root-level composables
 */
@Composable
fun OrielleThemeWithPreference(
    themeManager: com.orielle.ui.theme.ThemeManager,
    content: @Composable () -> Unit
) {
    val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = false)

    OrielleTheme(darkTheme = isDarkTheme) {
        content()
    }
}
