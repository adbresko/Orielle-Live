package com.orielle.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Represents a tag that can be associated with conversations.
 * Tags can be system-generated or user-created.
 */
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val userId: String,
    val usageCount: Int = 0,
    val createdAt: Date,
    val isUserCreated: Boolean = true, // false for system/suggested tags
    val color: String? = null, // Optional color for UI
    val description: String? = null // Optional description for complex tags
)
