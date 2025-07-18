package com.orielle.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Represents a mood check-in entry in the local Room database.
 * This class defines the schema for the 'mood_check_ins' table.
 *
 * @param id A unique identifier for the mood check-in entry.
 * @param userId The ID of the user who created this check-in.
 * @param mood The selected mood/emotion.
 * @param timestamp The date and time the check-in was created.
 * @param notes Optional notes associated with the mood check-in.
 */
@Entity(tableName = "mood_check_ins")
data class MoodCheckInEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val mood: String,
    val timestamp: Date,
    val notes: String? = null
)