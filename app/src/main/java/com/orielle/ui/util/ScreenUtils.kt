package com.orielle.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

object ScreenUtils {
    @Composable
    fun isSmallScreen(): Boolean {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp < 360 || configuration.screenHeightDp < 640
    }

    @Composable
    fun isMediumScreen(): Boolean {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp in 360..479 || configuration.screenHeightDp in 640..799
    }

    @Composable
    fun isLargeScreen(): Boolean {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp >= 480 || configuration.screenHeightDp >= 800
    }

    @Composable
    fun getScreenCategory(): ScreenCategory {
        return when {
            isSmallScreen() -> ScreenCategory.SMALL // Pixel 3/4 (U14/U15)
            isMediumScreen() -> ScreenCategory.MEDIUM // Pixel 9 Pro (U16)
            else -> ScreenCategory.LARGE // Tablets
        }
    }

    @Composable
    fun isPixel3Or4(): Boolean {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp < 360 || configuration.screenHeightDp < 640
    }

    @Composable
    fun isPixel9Pro(): Boolean {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp in 360..479 && configuration.screenHeightDp in 640..799
    }

    @Composable
    fun getResponsiveMultiplier(): Float {
        return when {
            isSmallScreen() -> 0.85f // Pixel 3/4 (U14/U15) - smaller
            isMediumScreen() -> 1.0f  // Pixel 9 Pro (U16) - current size (preserved)
            else -> 1.1f              // Larger screens - slightly larger
        }
    }
}

enum class ScreenCategory {
    SMALL,   // Pixel 3/4 (U14/U15)
    MEDIUM,  // Pixel 9 Pro (U16)
    LARGE    // Tablets
}
