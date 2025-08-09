package com.orielle.domain.repository

import com.orielle.domain.model.ChatConversation
import com.orielle.domain.model.ChatMessage
import com.orielle.domain.model.Response
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for chat conversations and messages.
 */
interface ChatRepository {

    // Conversation operations
    suspend fun saveConversation(conversation: ChatConversation): Response<String>
    fun getConversationsForUser(): Flow<Response<List<ChatConversation>>>
    suspend fun getConversationById(conversationId: String): Response<ChatConversation?>
    suspend fun deleteConversation(conversationId: String): Response<Unit>
    suspend fun updateConversationSavedStatus(conversationId: String, isSaved: Boolean): Response<Unit>

    // Message operations
    suspend fun saveMessage(message: ChatMessage): Response<String>
    fun getMessagesForConversation(conversationId: String): Flow<Response<List<ChatMessage>>>
    suspend fun deleteMessage(messageId: String): Response<Unit>
    suspend fun deleteAllMessagesInConversation(conversationId: String): Response<Unit>

    // Utility operations
    suspend fun updateConversationStats(conversationId: String): Response<Unit>
}
