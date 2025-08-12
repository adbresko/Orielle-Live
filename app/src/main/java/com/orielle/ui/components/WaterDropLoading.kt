package com.orielle.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.orielle.R

/**
 * Custom loading component that displays a pulsating water drop icon
 * instead of the basic CircularProgressIndicator
 * Uses native colors from ic_orielle_drop.xml for consistency
 */
@Composable
fun WaterDropLoading(
    modifier: Modifier = Modifier,
    size: Int = 80,
    tint: ColorFilter? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "water_drop_pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha_animation"
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_orielle_drop),
            contentDescription = "Loading water drop",
            modifier = Modifier
                .size(size.dp)
                .scale(scale)
                .alpha(alpha),
            colorFilter = tint // Only apply tint if explicitly provided, otherwise use native colors
        )
    }
}

/**
 * Compact version of the water drop loading for smaller spaces
 */
@Composable
fun WaterDropLoadingCompact(
    modifier: Modifier = Modifier,
    size: Int = 60,
    tint: ColorFilter? = null
) {
    WaterDropLoading(
        modifier = modifier,
        size = size,
        tint = tint
    )
}

/**
 * Large version of the water drop loading for prominent loading states
 */
@Composable
fun WaterDropLoadingLarge(
    modifier: Modifier = Modifier,
    size: Int = 120,
    tint: ColorFilter? = null
) {
    WaterDropLoading(
        modifier = modifier,
        size = size,
        tint = tint
    )
}
