package com.orielle.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.math.max

object ScreenUtils {
    @Composable
    fun isSmallScreen(): Boolean {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp < 400 || configuration.screenHeightDp < 700
    }

    @Composable
    fun isMediumScreen(): Boolean {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp in 400..599 || configuration.screenHeightDp in 700..899
    }

    @Composable
    fun isLargeScreen(): Boolean {
        val configuration = LocalConfiguration.current
        return configuration.screenWidthDp >= 600 || configuration.screenHeightDp >= 900
    }

    @Composable
    fun getScreenCategory(): ScreenCategory {
        return when {
            isSmallScreen() -> ScreenCategory.SMALL // Pixel 4 and similar compact phones
            isMediumScreen() -> ScreenCategory.MEDIUM // Pixel 9 Pro and similar phones
            else -> ScreenCategory.LARGE // Tablets and large phones
        }
    }

    @Composable
    fun isPixel4(): Boolean {
        val configuration = LocalConfiguration.current
        // Pixel 4 has 393x851 dp screen
        return configuration.screenWidthDp in 390..400 && configuration.screenHeightDp in 840..860
    }

    @Composable
    fun isPixel9Pro(): Boolean {
        val configuration = LocalConfiguration.current
        // Pixel 9 Pro has larger screen dimensions
        return configuration.screenWidthDp in 400..500 && configuration.screenHeightDp in 700..900
    }

    @Composable
    fun getResponsiveMultiplier(): Float {
        return when {
            isSmallScreen() -> 0.8f  // Smaller screens like Pixel 4
            isMediumScreen() -> 1.0f  // Medium screens like Pixel 9 Pro (baseline)
            else -> 1.2f              // Larger screens like tablets
        }
    }

    // Responsive dimension helpers
    @Composable
    fun responsivePadding(): Dp {
        val multiplier = getResponsiveMultiplier()
        return (16 * multiplier).dp
    }

    @Composable
    fun responsiveSpacing(): Dp {
        val multiplier = getResponsiveMultiplier()
        return (8 * multiplier).dp
    }

    @Composable
    fun responsiveIconSize(baseSize: Dp = 24.dp): Dp {
        val multiplier = getResponsiveMultiplier()
        return (baseSize.value * multiplier).dp
    }

    @Composable
    fun responsiveImageSize(baseSize: Dp = 60.dp): Dp {
        val multiplier = getResponsiveMultiplier()
        return (baseSize.value * multiplier).dp
    }

    @Composable
    fun responsiveCardPadding(): Dp {
        val multiplier = getResponsiveMultiplier()
        return (16 * multiplier).dp
    }

    @Composable
    fun responsiveButtonHeight(): Dp {
        val multiplier = getResponsiveMultiplier()
        return (48 * multiplier).dp
    }

    @Composable
    fun responsiveTextSpacing(): Dp {
        val multiplier = getResponsiveMultiplier()
        return (4 * multiplier).dp
    }

    // Screen-specific responsive values
    @Composable
    fun getResponsiveSizes(): Pair<Dp, Dp> {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val screenHeight = configuration.screenHeightDp.dp

        // Calculate responsive sizes based on screen dimensions
        val baseSize = min(screenWidth.value, screenHeight.value).dp * 0.12f // 12% of smaller dimension
        val iconSize = baseSize * 0.53f // Icon is about 53% of container size

        // Ensure minimum and maximum sizes for usability
        val containerSize = baseSize.coerceIn(40.dp, 80.dp)
        val iconSizeFinal = iconSize.coerceIn(20.dp, 40.dp)

        return Pair(containerSize, iconSizeFinal)
    }

    // Typography scaling
    @Composable
    fun getTextScaleFactor(): Float {
        return when {
            isSmallScreen() -> 0.9f
            isMediumScreen() -> 1.0f
            else -> 1.1f
        }
    }
}

enum class ScreenCategory {
    SMALL,   // Pixel 4 and similar compact phones
    MEDIUM,  // Pixel 9 Pro and similar phones
    LARGE    // Tablets and large phones
}
