package com.orielle.domain.model

import java.util.Date

/**
 * Domain model representing a chat conversation.
 */
data class ChatConversation(
    val id: String,
    val userId: String,
    val title: String? = null, // Auto-generated from first message or user-defined
    val createdAt: Date,
    val updatedAt: Date,
    val tags: List<String> = emptyList(), // List of tag names/IDs
    val messageCount: Int = 0,
    val isSaved: Boolean = false, // Whether user explicitly saved this conversation
    val lastMessagePreview: String? = null // For showing in conversation list
)
