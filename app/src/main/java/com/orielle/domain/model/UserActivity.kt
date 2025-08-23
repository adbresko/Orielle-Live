package com.orielle.domain.model

import java.util.Date

/**
 * Represents a user activity that can be displayed on the Remember screen calendar.
 * This tracks when users reflect, ask questions, or check in their mood.
 */
data class UserActivity(
    val id: String = "",
    val userId: String = "",
    val activityType: ActivityType,
    val timestamp: Date = Date(),
    val relatedId: String? = null, // ID of the related journal entry, conversation, or mood check-in
    val title: String? = null,
    val preview: String? = null,
    val tags: List<String> = emptyList(),
    val mood: String? = null
)

/**
 * Enum representing different types of user activities.
 */
enum class ActivityType {
    REFLECT,    // Journal entry or reflection
    ASK,        // Chat conversation or question
    CHECK_IN    // Mood check-in
}

/**
 * Represents a calendar day with its activities and visual indicators.
 */
data class CalendarDay(
    val date: Date,
    val dayOfMonth: Int,
    val isToday: Boolean = false,
    val activities: List<UserActivity> = emptyList(),
    val hasReflectActivity: Boolean = false,
    val hasAskActivity: Boolean = false,
    val hasCheckInActivity: Boolean = false
)
