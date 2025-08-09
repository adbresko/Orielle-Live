package com.orielle.data.local.dao

import androidx.room.*
import com.orielle.data.local.model.TagEntity
import com.orielle.data.local.model.ConversationTagCrossRef
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for tags and conversation-tag relationships.
 */
@Dao
interface TagDao {

    @Upsert
    suspend fun upsertTag(tag: TagEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertConversationTagCrossRef(crossRef: ConversationTagCrossRef)

    @Delete
    suspend fun deleteConversationTagCrossRef(crossRef: ConversationTagCrossRef)

    @Query("SELECT * FROM tags WHERE userId = :userId ORDER BY usageCount DESC, name ASC")
    fun getTagsForUser(userId: String): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE userId = :userId AND isUserCreated = 0 ORDER BY name ASC")
    fun getSuggestedTags(userId: String): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE userId = :userId AND isUserCreated = 1 ORDER BY usageCount DESC, name ASC")
    fun getUserCreatedTags(userId: String): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE name = :name AND userId = :userId LIMIT 1")
    suspend fun getTagByName(name: String, userId: String): TagEntity?

    @Query("UPDATE tags SET usageCount = usageCount + 1 WHERE id = :tagId")
    suspend fun incrementTagUsage(tagId: String)

    @Query("UPDATE tags SET usageCount = usageCount - 1 WHERE id = :tagId AND usageCount > 0")
    suspend fun decrementTagUsage(tagId: String)

    @Query("DELETE FROM tags WHERE id = :tagId")
    suspend fun deleteTag(tagId: String)

    @Query("DELETE FROM conversation_tag_cross_ref WHERE conversationId = :conversationId")
    suspend fun removeAllTagsFromConversation(conversationId: String)

    // Get most popular tags for suggestions
    @Query("SELECT * FROM tags WHERE userId = :userId ORDER BY usageCount DESC LIMIT :limit")
    suspend fun getMostPopularTags(userId: String, limit: Int): List<TagEntity>
}
