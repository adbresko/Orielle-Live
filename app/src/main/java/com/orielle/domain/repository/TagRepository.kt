package com.orielle.domain.repository

import com.orielle.domain.model.Tag
import com.orielle.domain.model.Response
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for tag operations.
 */
interface TagRepository {

    // Tag operations
    suspend fun saveTag(tag: Tag): Response<String>
    fun getTagsForUser(): Flow<Response<List<Tag>>>
    fun getSuggestedTags(): Flow<Response<List<Tag>>>
    fun getUserCreatedTags(): Flow<Response<List<Tag>>>
    suspend fun deleteTag(tagId: String): Response<Unit>
    suspend fun getTagByName(name: String): Response<Tag?>

    // Tag-Conversation relationship operations
    suspend fun addTagToConversation(conversationId: String, tagId: String): Response<Unit>
    suspend fun removeTagFromConversation(conversationId: String, tagId: String): Response<Unit>
    suspend fun removeAllTagsFromConversation(conversationId: String): Response<Unit>
    suspend fun getTagsForConversation(conversationId: String): Response<List<Tag>>

    // Tag usage tracking
    suspend fun incrementTagUsage(tagId: String): Response<Unit>
    suspend fun decrementTagUsage(tagId: String): Response<Unit>
}
