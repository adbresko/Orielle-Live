package com.orielle.data.mapper

import com.orielle.data.local.model.ChatConversationEntity
import com.orielle.data.local.model.ChatMessageEntity
import com.orielle.domain.model.ChatConversation
import com.orielle.domain.model.ChatMessage

/**
 * Mapper functions to convert between Chat entities and domain models.
 */

// ChatConversation mappers
fun ChatConversationEntity.toDomain(): ChatConversation {
    return ChatConversation(
        id = id,
        userId = userId,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt,
        tags = tags,
        messageCount = messageCount,
        isSaved = isSaved,
        lastMessagePreview = lastMessagePreview
    )
}

fun ChatConversation.toEntity(): ChatConversationEntity {
    return ChatConversationEntity(
        id = id,
        userId = userId,
        title = title,
        createdAt = createdAt,
        updatedAt = updatedAt,
        tags = tags,
        messageCount = messageCount,
        isSaved = isSaved,
        lastMessagePreview = lastMessagePreview
    )
}

// ChatMessage mappers
fun ChatMessageEntity.toDomain(): ChatMessage {
    return ChatMessage(
        id = id,
        conversationId = conversationId,
        content = content,
        isFromUser = isFromUser,
        timestamp = timestamp,
        messageType = messageType,
        metadata = metadata
    )
}

fun ChatMessage.toEntity(): ChatMessageEntity {
    return ChatMessageEntity(
        id = id,
        conversationId = conversationId,
        content = content,
        isFromUser = isFromUser,
        timestamp = timestamp,
        messageType = messageType,
        metadata = metadata
    )
}
