package com.orielle.data.local.dao

import androidx.room.*
import com.orielle.data.local.model.ChatMessageEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ChatMessageDao {

    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp DESC")
    fun getChatMessagesForUser(userId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE userId = :userId AND conversationId = :conversationId ORDER BY timestamp ASC")
    fun getMessagesForConversation(userId: String, conversationId: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(chatMessage: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessages(chatMessages: List<ChatMessageEntity>)

    @Delete
    suspend fun deleteChatMessage(chatMessage: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE userId = :userId AND conversationId = :conversationId")
    suspend fun deleteConversation(userId: String, conversationId: String)

    @Query("SELECT DISTINCT conversationId FROM chat_messages WHERE userId = :userId ORDER BY timestamp DESC")
    fun getConversationIds(userId: String): Flow<List<String>>

    @Query("SELECT * FROM chat_messages WHERE userId = :userId AND timestamp >= :since ORDER BY timestamp DESC")
    fun getRecentMessages(userId: String, since: Date): Flow<List<ChatMessageEntity>>
}