package com.orielle.ui.components

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

/**
 * A standardized custom shape that creates the gentle "half-moon" dip at the top.
 * This is the single source of truth for this background style.
 */
class HalfMoonShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            // This logic is now standardized from the working WelcomeScreen
            moveTo(0f, size.height * 0.15f)
            quadraticBezierTo(
                x1 = size.width / 2, y1 = -size.height * 0.1f,
                x2 = size.width, y2 = size.height * 0.15f
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}