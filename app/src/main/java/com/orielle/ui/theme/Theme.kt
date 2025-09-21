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
    surfaceVariant = DarkGray.copy(alpha = 0.8f), // Slightly lighter variant
    onSurfaceVariant = White.copy(alpha = 0.8f),
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
    surfaceVariant = SoftSand.copy(alpha = 0.8f), // Slightly darker variant
    onSurfaceVariant = Charcoal.copy(alpha = 0.8f),
    error = ErrorRed,
    onError = White
)

@Composable
fun OrielleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Use system preference by default
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val responsiveTypography = getResponsiveTypography()

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar and navigation bar colors (API 21+)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                // Make status bar transparent for edge-to-edge
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
    val isDark = isDarkTheme()
    return if (isDark) {
        BorderStroke(1.dp, MediumGray)
    } else {
        BorderStroke(1.dp, LightGray)
    }
}

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

/**
 * Utility function to consistently detect if the current theme is dark mode
 * This replaces all the inconsistent dark mode detection patterns across screens
 */
@Composable
fun isDarkTheme(): Boolean {
    return MaterialTheme.colorScheme.background == DarkGray
}

/**
 * Utility function to get theme-aware colors consistently
 * This replaces manual color calculations across screens
 */
@Composable
fun getThemeColors(): ThemeColors {
    val isDark = isDarkTheme()
    return ThemeColors(
        background = MaterialTheme.colorScheme.background,
        onBackground = MaterialTheme.colorScheme.onBackground,
        surface = MaterialTheme.colorScheme.surface,
        onSurface = MaterialTheme.colorScheme.onSurface,
        primary = MaterialTheme.colorScheme.primary,
        onPrimary = MaterialTheme.colorScheme.onPrimary,
        secondary = MaterialTheme.colorScheme.secondary,
        onSecondary = MaterialTheme.colorScheme.onSecondary,
        isDark = isDark
    )
}

/**
 * Data class to hold all theme-aware colors for consistent usage
 */
data class ThemeColors(
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val isDark: Boolean
)
