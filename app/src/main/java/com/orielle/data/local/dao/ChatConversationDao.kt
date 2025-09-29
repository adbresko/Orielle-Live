package com.orielle.data.local.dao

import androidx.room.*
import com.orielle.data.local.model.ChatConversationEntity
import com.orielle.data.local.model.ConversationTagCrossRef
import com.orielle.data.local.model.TagEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for chat conversations.
 */
@Dao
interface ChatConversationDao {

    @Upsert
    suspend fun upsertConversation(conversation: ChatConversationEntity)

    @Query("SELECT * FROM chat_conversations WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getConversationsForUser(userId: String): Flow<List<ChatConversationEntity>>

    @Query("SELECT * FROM chat_conversations WHERE id = :conversationId")
    suspend fun getConversationById(conversationId: String): ChatConversationEntity?

    @Query("DELETE FROM chat_conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)

    @Query("UPDATE chat_conversations SET messageCount = :count, updatedAt = :updatedAt, lastMessagePreview = :preview WHERE id = :conversationId")
    suspend fun updateConversationStats(conversationId: String, count: Int, updatedAt: java.util.Date, preview: String?)

    @Query("UPDATE chat_conversations SET isSaved = :isSaved WHERE id = :conversationId")
    suspend fun updateSavedStatus(conversationId: String, isSaved: Boolean)

    @Query("UPDATE chat_conversations SET title = :title WHERE id = :conversationId")
    suspend fun updateTitle(conversationId: String, title: String)

    @Query("UPDATE chat_conversations SET tags = :tags WHERE id = :conversationId")
    suspend fun updateTags(conversationId: String, tags: List<String>)

    // Get conversations with their tags
    @Transaction
    @Query("SELECT * FROM chat_conversations WHERE userId = :userId ORDER BY updatedAt DESC")
    fun getConversationsWithTags(userId: String): Flow<List<ConversationWithTags>>

    @Query("SELECT * FROM chat_conversations WHERE userId = :userId AND isSaved = 1 ORDER BY updatedAt DESC")
    fun getSavedConversations(userId: String): Flow<List<ChatConversationEntity>>
}

/**
 * Data class to represent a conversation with its associated tags.
 */
data class ConversationWithTags(
    @Embedded val conversation: ChatConversationEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            ConversationTagCrossRef::class,
            parentColumn = "conversationId",
            entityColumn = "tagId"
        )
    )
    val tags: List<TagEntity>
)
