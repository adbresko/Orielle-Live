package com.orielle.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

/**
 * Predefined color palette for avatar backgrounds
 * Following modern app design patterns with accessible, vibrant colors
 */
val avatarColorPalette = listOf(
    // Primary brand colors
    Color(0xFF6B73FF), // Orielle Blue
    Color(0xFF9C88FF), // Purple
    Color(0xFF4ECDC4), // Teal
    Color(0xFF45B7D1), // Sky Blue

    // Warm colors
    Color(0xFFFF6B6B), // Coral
    Color(0xFFFF9F43), // Orange
    Color(0xFFFFD93D), // Yellow
    Color(0xFF6BCF7F), // Green

    // Cool colors
    Color(0xFF4D96FF), // Blue
    Color(0xFF9775FA), // Violet
    Color(0xFF20BF6B), // Emerald
    Color(0xFF26DE81), // Mint

    // Neutral colors
    Color(0xFF95A5A6), // Gray
    Color(0xFF2C3E50), // Dark Blue Gray
    Color(0xFFE17055), // Burnt Orange
    Color(0xFFA29BFE), // Light Purple

    // Premium colors
    Color(0xFFFF7675), // Rose
    Color(0xFF74B9FF), // Light Blue
    Color(0xFF00B894), // Turquoise
    Color(0xFFE84393), // Pink
)

@Composable
fun ColorPicker(
    selectedColor: String?,
    onColorSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Choose Background Color",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(200.dp)
        ) {
            items(avatarColorPalette) { color ->
                val colorHex = color.toArgb().toString(16).let { hex ->
                    "#${hex.drop(2)}" // Remove alpha and add #
                }
                val isSelected = selectedColor == colorHex

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(colorHex) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        Text(
            text = "Tap a color to set as your avatar background",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Helper function to convert hex string to Color
 */
fun hexToColor(hex: String): Color {
    return try {
        val cleanHex = hex.removePrefix("#")
        val colorValue = if (cleanHex.length == 6) {
            "FF$cleanHex" // Add alpha
        } else {
            cleanHex
        }
        Color(colorValue.toLong(16))
    } catch (e: Exception) {
        Color(0xFF6B73FF) // Default Orielle blue
    }
}
