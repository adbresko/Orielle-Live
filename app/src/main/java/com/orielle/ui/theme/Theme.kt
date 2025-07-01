package com.orielle.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// This defines the roles for your colors
private val LightColorScheme = lightColorScheme(
    primary = OriellePrimary700,
    secondary = OrielleSecondary500,
    background = OrielleBackground,
    surface = OrielleSurface,
    onPrimary = OrielleSurface, // Text on a primary color background should be white
    onSecondary = OrielleCharcoal, // Text on a secondary color background
    onBackground = OrielleCharcoal, // Text on the main background
    onSurface = OrielleCharcoal // Text on cards and other surfaces
)

@Composable
fun OrielleTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb() // Status bar matches background
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = OrielleTypography, // We will define this in the next step
        content = content
    )
}