package com.orielle.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.Index
import java.util.Date

/**
 * Represents a single chat message in the local Room database.
 * Each message belongs to a conversation and can be from user or Orielle.
 */
@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatConversationEntity::class,
            parentColumns = ["id"],
            childColumns = ["conversationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["conversationId"]),
        Index(value = ["timestamp"])
    ]
)
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val conversationId: String,
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Date,
    val messageType: String = "text", // "text", "voice", "image" for future expansion
    val metadata: String? = null // JSON string for additional message data
)