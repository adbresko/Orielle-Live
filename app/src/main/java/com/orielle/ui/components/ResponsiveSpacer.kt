package com.orielle.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp

@Composable
fun ResponsiveSpacer(
    size: androidx.compose.ui.unit.Dp = 16.dp
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp

    // Scale spacing for smaller screens - Pixel 3/4 gets smaller spacing, Pixel 9 Pro keeps current
    val scaledSize = when {
        screenHeight < 640 -> size * 0.75f // Pixel 3/4 (U14/U15) - smaller
        screenHeight < 800 -> size         // Pixel 9 Pro (U16) - current size (preserved)
        else -> size * 1.1f               // Larger screens - slightly larger
    }

    Spacer(modifier = Modifier.height(scaledSize))
}

@Composable
fun ResponsiveHorizontalSpacer(
    size: androidx.compose.ui.unit.Dp = 16.dp
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    // Scale spacing for smaller screens - Pixel 3/4 gets smaller spacing, Pixel 9 Pro keeps current
    val scaledSize = when {
        screenWidth < 360 -> size * 0.75f // Pixel 3/4 (U14/U15) - smaller
        screenWidth < 480 -> size         // Pixel 9 Pro (U16) - current size (preserved)
        else -> size * 1.1f              // Larger screens - slightly larger
    }

    Spacer(modifier = Modifier.width(scaledSize))
}

@Composable
fun ResponsiveSpacerSmall() = ResponsiveSpacer(8.dp)
@Composable
fun ResponsiveSpacerMedium() = ResponsiveSpacer(16.dp)
@Composable
fun ResponsiveSpacerLarge() = ResponsiveSpacer(24.dp)
@Composable
fun ResponsiveSpacerXLarge() = ResponsiveSpacer(32.dp)
