package com.orielle.domain.model

import java.util.Date

/**
 * Domain model representing a single chat message.
 */
data class ChatMessage(
    val id: String = "",
    val conversationId: String = "",
    val content: String = "",
    val isFromUser: Boolean = false,
    val timestamp: Date = Date(),
    val messageType: String = "text", // "text", "voice", "image" for future expansion
    val metadata: String? = null // JSON string for additional message data
) {
    // No-argument constructor for Firebase Firestore deserialization
    constructor() : this("", "", "", false, Date(), "text", null)
}
