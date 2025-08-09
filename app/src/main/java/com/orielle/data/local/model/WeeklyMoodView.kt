package com.orielle.domain.model

import java.util.Date

/**
 * Represents a single day in the weekly mood view.
 */
data class DayMoodData(
    val dayLabel: String, // "M", "T", "W", etc.
    val date: Date,
    val moodCheckIn: MoodCheckIn?, // null if no check-in for this day
    val isToday: Boolean = false
)

/**
 * Represents the weekly mood view data for the home screen.
 */
data class WeeklyMoodView(
    val days: List<DayMoodData>,
    val todayIndex: Int // Index of today in the days list
)

/**
 * Enum representing different mood types with their corresponding icon resources.
 */
enum class MoodType(val displayName: String, val iconResId: Int) {
    HAPPY("Happy", com.orielle.R.drawable.ic_happy),
    PEACEFUL("Peaceful", com.orielle.R.drawable.ic_peaceful),
    PLAYFUL("Playful", com.orielle.R.drawable.ic_playful),
    FRUSTRATED("Frustrated", com.orielle.R.drawable.ic_frustrated),
    ANGRY("Angry", com.orielle.R.drawable.ic_angry),
    SAD("Sad", com.orielle.R.drawable.ic_sad),
    SCARED("Scared", com.orielle.R.drawable.ic_scared),
    SHY("Shy", com.orielle.R.drawable.ic_shy),
    SURPRISED("Surprised", com.orielle.R.drawable.ic_surprised);

    companion object {
        fun fromString(mood: String): MoodType? {
            return values().find { it.displayName.equals(mood, ignoreCase = true) }
        }
    }
}
