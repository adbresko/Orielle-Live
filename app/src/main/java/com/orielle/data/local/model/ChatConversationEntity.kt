package com.orielle.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.orielle.data.local.Converters
import java.util.Date

/**
 * Represents a chat conversation in the local Room database.
 * Each conversation contains multiple chat messages and can have tags.
 */
@Entity(tableName = "chat_conversations")
@TypeConverters(Converters::class)
data class ChatConversationEntity(
    @PrimaryKey
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
