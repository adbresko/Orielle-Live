package com.orielle.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a mood tag or category that can be associated with check-ins.
 * This class defines the schema for the 'mood_tags' table.
 *
 * @param id A unique identifier for the mood tag.
 * @param name The name of the mood tag (e.g., "Anxious", "Grateful", "Stressed").
 * @param color The color associated with this mood tag.
 * @param icon The icon resource ID for this mood tag.
 * @param isCustom Whether this is a custom tag created by the user.
 * @param userId The ID of the user who created this tag (null for default tags).
 */
@Entity(tableName = "mood_tags")
data class MoodTagEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val color: String, // Hex color code
    val icon: String, // Icon identifier
    val isCustom: Boolean = false,
    val userId: String? = null
)