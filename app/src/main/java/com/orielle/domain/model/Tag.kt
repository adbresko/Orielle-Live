package com.orielle.domain.model

import java.util.Date

/**
 * Domain model representing a tag for conversations.
 */
data class Tag(
    val id: String,
    val name: String,
    val userId: String,
    val usageCount: Int = 0,
    val createdAt: Date,
    val isUserCreated: Boolean = true, // false for system/suggested tags
    val color: String? = null, // Optional color for UI
    val description: String? = null // Optional description for complex tags
)
