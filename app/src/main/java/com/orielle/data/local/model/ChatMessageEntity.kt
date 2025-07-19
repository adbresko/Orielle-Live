package com.orielle.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Represents a chat message in the conversation with Orielle.
 * This class defines the schema for the 'chat_messages' table.
 *
 * @param id A unique identifier for the chat message.
 * @param userId The ID of the user who sent/received this message.
 * @param message The text content of the message.
 * @param timestamp The date and time the message was sent.
 * @param isFromUser Whether this message is from the user (true) or Orielle (false).
 * @param conversationId A unique identifier for grouping messages in a conversation.
 * @param moodContext Optional mood context when the message was sent.
 */
@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val message: String,
    val timestamp: Date,
    val isFromUser: Boolean,
    val conversationId: String,
    val moodContext: String? = null
)