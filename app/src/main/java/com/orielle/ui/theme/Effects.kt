package com.orielle.ui.theme

import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * A custom RippleTheme to create a "water drop" or "energy absorption" effect
 * that aligns with Orielle's brand identity.
 */
object WaterRippleTheme : RippleTheme {
    private const val PRESS_ALPHA = 0.10f
    private const val FOCUS_ALPHA = 0.12f
    private const val HOVER_ALPHA = 0.04f
    private const val DRAG_ALPHA = 0.16f

    // The color of the ripple will be based on the primary theme color.
    @Composable
    override fun defaultColor(): Color = MaterialTheme.colorScheme.primary

    @Composable
    override fun rippleAlpha(): RippleAlpha = RippleAlpha(
        pressedAlpha = PRESS_ALPHA,
        focusedAlpha = FOCUS_ALPHA,
        hoveredAlpha = HOVER_ALPHA,
        draggedAlpha = DRAG_ALPHA
    )
}
