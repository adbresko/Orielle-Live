package com.orielle.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

object Dimensions {
    // Base dimensions that scale with screen size
    val paddingSmall = 8.dp
    val paddingMedium = 16.dp
    val paddingLarge = 24.dp
    val paddingXLarge = 32.dp

    // Responsive dimensions
    @Composable
    fun responsivePadding(): ResponsivePadding {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp

        return when {
            screenWidth < 360 -> ResponsivePadding(12.dp, 16.dp, 20.dp, 24.dp) // Small phones (Pixel 3/4)
            screenWidth < 480 -> ResponsivePadding(16.dp, 20.dp, 24.dp, 28.dp) // Medium phones
            screenWidth < 600 -> ResponsivePadding(20.dp, 24.dp, 28.dp, 32.dp) // Large phones (Pixel 9 Pro)
            else -> ResponsivePadding(24.dp, 28.dp, 32.dp, 36.dp) // Tablets
        }
    }

    @Composable
    fun responsiveSpacing(): ResponsiveSpacing {
        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp

        return when {
            screenHeight < 640 -> ResponsiveSpacing(8.dp, 12.dp, 16.dp, 20.dp) // U14/U15 phones (Pixel 3/4)
            screenHeight < 800 -> ResponsiveSpacing(12.dp, 16.dp, 20.dp, 24.dp) // U16 phones
            else -> ResponsiveSpacing(16.dp, 20.dp, 24.dp, 28.dp) // Tablets
        }
    }

    // Screen-specific dimensions that maintain current Pixel 9 Pro experience
    @Composable
    fun screenSpecificPadding(): ScreenSpecificPadding {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp
        val screenHeight = configuration.screenHeightDp

        return when {
            // Pixel 3/4 (U14/U15) - smaller dimensions
            screenWidth < 360 || screenHeight < 640 -> ScreenSpecificPadding(
                horizontal = 16.dp,
                vertical = 12.dp,
                top = 32.dp,
                bottom = 16.dp
            )
            // Pixel 9 Pro (U16) - current dimensions (preserved)
            screenWidth < 480 || screenHeight < 800 -> ScreenSpecificPadding(
                horizontal = 24.dp,
                vertical = 16.dp,
                top = 60.dp,
                bottom = 24.dp
            )
            // Larger screens - slightly increased
            else -> ScreenSpecificPadding(
                horizontal = 28.dp,
                vertical = 20.dp,
                top = 72.dp,
                bottom = 28.dp
            )
        }
    }
}

data class ResponsivePadding(
    val small: androidx.compose.ui.unit.Dp,
    val medium: androidx.compose.ui.unit.Dp,
    val large: androidx.compose.ui.unit.Dp,
    val xLarge: androidx.compose.ui.unit.Dp
)

data class ResponsiveSpacing(
    val small: androidx.compose.ui.unit.Dp,
    val medium: androidx.compose.ui.unit.Dp,
    val large: androidx.compose.ui.unit.Dp,
    val xLarge: androidx.compose.ui.unit.Dp
)

data class ScreenSpecificPadding(
    val horizontal: androidx.compose.ui.unit.Dp,
    val vertical: androidx.compose.ui.unit.Dp,
    val top: androidx.compose.ui.unit.Dp,
    val bottom: androidx.compose.ui.unit.Dp
)
