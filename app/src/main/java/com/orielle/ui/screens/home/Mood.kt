package com.orielle.ui.screens.home

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.orielle.R
import com.orielle.ui.theme.*

// Data class to hold all properties for a single mood
data class Mood(
    val name: String,
    @DrawableRes val iconResId: Int,
    val color: Color
)

// A predefined list of all moods for easy use in the UI.
// This uses the colors you have defined in your Color.kt file.
val allMoods = listOf(
    Mood("Clear", R.drawable.ic_mood_clear, AuroraGold), // A sunny, golden yellow
    Mood("Partly Cloudy", R.drawable.ic_mood_partly_cloudy, Color(0xFFB0BEC5)), // A neutral gray
    Mood("Foggy", R.drawable.ic_mood_foggy, Color(0xFFCFD8DC)), // A lighter gray
    Mood("Overcast", R.drawable.ic_mood_overcast, Color(0xFF90A4AE)), // A darker gray
    Mood("Rainy", R.drawable.ic_mood_rainy, WaterBlue), // Your brand's WaterBlue
    Mood("Stormy", R.drawable.ic_mood_stormy, Color(0xFF42A5F5)), // A deeper blue
    Mood("Frozen", R.drawable.ic_mood_frozen, Color(0xFF81D4FA)), // An icy blue
    Mood("Rainbow", R.drawable.ic_mood_rainbow, StillwaterTeal) // Your brand's Teal
)