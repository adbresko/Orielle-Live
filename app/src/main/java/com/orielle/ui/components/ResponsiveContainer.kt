package com.orielle.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.orielle.ui.theme.Dimensions

@Composable
fun ResponsiveContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    val screenHeight = configuration.screenHeightDp

    // Determine if this is a small screen (Pixel 3/4 - U14/U15)
    val isSmallScreen = screenWidth < 360 || screenHeight < 640

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = if (isSmallScreen) 16.dp else 24.dp,
                vertical = if (isSmallScreen) 12.dp else 16.dp
            )
    ) {
        content()
    }
}

@Composable
fun ResponsiveHorizontalPadding(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    // Pixel 3/4 gets smaller padding, Pixel 9 Pro keeps current padding
    val horizontalPadding = when {
        screenWidth < 360 -> 16.dp  // Pixel 3/4 (U14/U15)
        screenWidth < 480 -> 24.dp  // Pixel 9 Pro (U16) - current size
        else -> 28.dp               // Larger screens
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
    ) {
        content()
    }
}

@Composable
fun ResponsiveVerticalPadding(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp

    // Pixel 3/4 gets smaller padding, Pixel 9 Pro keeps current padding
    val verticalPadding = when {
        screenHeight < 640 -> 12.dp  // Pixel 3/4 (U14/U15)
        screenHeight < 800 -> 16.dp  // Pixel 9 Pro (U16) - current size
        else -> 20.dp                // Larger screens
    }

    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = verticalPadding)
    ) {
        content()
    }
}
