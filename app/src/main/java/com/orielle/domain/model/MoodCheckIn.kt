package com.orielle.domain.model

import java.util.Date

/**
 * Domain model representing a mood check-in entry.
 * This is the clean domain model used throughout the app.
 *
 * @param id Unique identifier for the mood check-in.
 * @param userId ID of the user who created this check-in.
 * @param mood The selected mood/emotion.
 * @param timestamp When the check-in was created.
 * @param notes Optional notes associated with the mood check-in.
 */
data class MoodCheckIn(
    val id: String,
    val userId: String,
    val mood: String,
    val timestamp: Date,
    val notes: String? = null
)