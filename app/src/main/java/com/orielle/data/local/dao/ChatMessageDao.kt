package com.orielle.data.local.dao

import androidx.room.*
import com.orielle.data.local.model.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for chat messages.
 */
@Dao
interface ChatMessageDao {

    @Upsert
    suspend fun upsertMessage(message: ChatMessageEntity)

    @Insert
    suspend fun insertMessages(messages: List<ChatMessageEntity>)

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): ChatMessageEntity?

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)

    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun deleteAllMessagesInConversation(conversationId: String)

    @Query("SELECT COUNT(*) FROM chat_messages WHERE conversationId = :conversationId")
    suspend fun getMessageCount(conversationId: String): Int

    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestMessage(conversationId: String): ChatMessageEntity?

    @Query("SELECT content FROM chat_messages WHERE conversationId = :conversationId AND isFromUser = 1 ORDER BY timestamp ASC LIMIT 1")
    suspend fun getFirstUserMessage(conversationId: String): String?
}