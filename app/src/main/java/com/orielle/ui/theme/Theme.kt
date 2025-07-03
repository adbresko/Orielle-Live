package com.orielle.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Maps the brand colors to the Material 3 Light Color Scheme.
private val LightColorScheme = lightColorScheme(
    primary = WaterBlue,
    secondary = StillwaterTeal,
    tertiary = AuroraGold,
    background = SoftSand,
    surface = SoftSand,
    onPrimary = Charcoal,
    onSecondary = SoftSand,
    onTertiary = Charcoal,
    onBackground = Charcoal,
    onSurface = Charcoal,
    error = PetalCoral,
    onError = SoftSand
)

// Maps the brand colors to the Material 3 Dark Color Scheme.
private val DarkColorScheme = darkColorScheme(
    primary = WaterBlue,
    secondary = StillwaterTeal,
    tertiary = AuroraGold,
    background = DeepSpace,
    surface = DeepSpace,
    onPrimary = DeepSpace,
    onSecondary = DeepSpace,
    onTertiary = DeepSpace,
    onBackground = SoftSand,
    onSurface = SoftSand,
    error = PetalCoral,
    onError = DeepSpace
)

@Composable
fun OrielleTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disabled to enforce our specific brand colors
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb() // Use background color for status bar
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
