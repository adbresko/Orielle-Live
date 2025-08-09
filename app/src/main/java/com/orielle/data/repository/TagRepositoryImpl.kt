package com.orielle.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.orielle.data.local.dao.TagDao
import com.orielle.data.local.model.ConversationTagCrossRef
import com.orielle.data.mapper.toDomain
import com.orielle.data.mapper.toEntity
import com.orielle.domain.manager.SessionManager
import com.orielle.domain.model.AppError
import com.orielle.domain.model.Tag
import com.orielle.domain.model.Response
import com.orielle.domain.repository.TagRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val tagDao: TagDao,
    private val firestore: FirebaseFirestore,
    private val sessionManager: SessionManager
) : TagRepository {

    override suspend fun saveTag(tag: Tag): Response<String> {
        return try {
            val userId = sessionManager.currentUserId.first()
                ?: return Response.Failure(AppError.Auth, Exception("No user session found."))
            val isGuest = sessionManager.isGuest.first()

            val tagToSave = tag.copy(
                userId = userId,
                id = if (tag.id.isBlank()) UUID.randomUUID().toString() else tag.id
            )

            // Save to local database first
            tagDao.upsertTag(tagToSave.toEntity())

            // Sync to Firebase if not a guest user
            if (!isGuest) {
                firestore.collection("users").document(userId)
                    .collection("tags").document(tagToSave.id)
                    .set(tagToSave).await()
            }

            Response.Success(tagToSave.id)
        } catch (e: Exception) {
            Timber.e(e, "Error saving tag")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    override fun getTagsForUser(): Flow<Response<List<Tag>>> {
        return sessionManager.currentUserId.map { userId ->
            if (userId == null) {
                Response.Success(emptyList<Tag>())
            } else {
                try {
                    tagDao.getTagsForUser(userId).map { entities ->
                        Response.Success(entities.map { it.toDomain() })
                    }.first()
                } catch (e: Exception) {
                    Timber.e(e, "Error fetching tags for user")
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Response.Failure(AppError.Database, e)
                }
            }
        }
    }

    override fun getSuggestedTags(): Flow<Response<List<Tag>>> {
        return sessionManager.currentUserId.map { userId ->
            if (userId == null) {
                Response.Success(emptyList<Tag>())
            } else {
                try {
                    tagDao.getSuggestedTags(userId).map { entities ->
                        Response.Success(entities.map { it.toDomain() })
                    }.first()
                } catch (e: Exception) {
                    Timber.e(e, "Error fetching suggested tags")
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Response.Failure(AppError.Database, e)
                }
            }
        }
    }

    override fun getUserCreatedTags(): Flow<Response<List<Tag>>> {
        return sessionManager.currentUserId.map { userId ->
            if (userId == null) {
                Response.Success(emptyList<Tag>())
            } else {
                try {
                    tagDao.getUserCreatedTags(userId).map { entities ->
                        Response.Success(entities.map { it.toDomain() })
                    }.first()
                } catch (e: Exception) {
                    Timber.e(e, "Error fetching user created tags")
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Response.Failure(AppError.Database, e)
                }
            }
        }
    }

    override suspend fun deleteTag(tagId: String): Response<Unit> {
        return try {
            val userId = sessionManager.currentUserId.first()
                ?: return Response.Failure(AppError.Auth, Exception("No user session found."))
            val isGuest = sessionManager.isGuest.first()

            // Delete from local database
            tagDao.deleteTag(tagId)

            // Delete from Firebase if not a guest user
            if (!isGuest) {
                firestore.collection("users").document(userId)
                    .collection("tags").document(tagId)
                    .delete().await()
            }

            Response.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting tag")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    override suspend fun getTagByName(name: String): Response<Tag?> {
        return try {
            val userId = sessionManager.currentUserId.first()
                ?: return Response.Failure(AppError.Auth, Exception("No user session found."))

            val entity = tagDao.getTagByName(name, userId)
            Response.Success(entity?.toDomain())
        } catch (e: Exception) {
            Timber.e(e, "Error getting tag by name")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    override suspend fun addTagToConversation(conversationId: String, tagId: String): Response<Unit> {
        return try {
            val crossRef = ConversationTagCrossRef(conversationId, tagId)
            tagDao.insertConversationTagCrossRef(crossRef)
            tagDao.incrementTagUsage(tagId)
            Response.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error adding tag to conversation")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    override suspend fun removeTagFromConversation(conversationId: String, tagId: String): Response<Unit> {
        return try {
            val crossRef = ConversationTagCrossRef(conversationId, tagId)
            tagDao.deleteConversationTagCrossRef(crossRef)
            tagDao.decrementTagUsage(tagId)
            Response.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error removing tag from conversation")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    override suspend fun removeAllTagsFromConversation(conversationId: String): Response<Unit> {
        return try {
            tagDao.removeAllTagsFromConversation(conversationId)
            Response.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error removing all tags from conversation")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    override suspend fun getTagsForConversation(conversationId: String): Response<List<Tag>> {
        return try {
            // This would require a more complex query - for now, return empty list
            // In a real implementation, you'd need to join the conversation_tag_cross_ref table
            Response.Success(emptyList())
        } catch (e: Exception) {
            Timber.e(e, "Error getting tags for conversation")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    override suspend fun incrementTagUsage(tagId: String): Response<Unit> {
        return try {
            tagDao.incrementTagUsage(tagId)
            Response.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error incrementing tag usage")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    override suspend fun decrementTagUsage(tagId: String): Response<Unit> {
        return try {
            tagDao.decrementTagUsage(tagId)
            Response.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error decrementing tag usage")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }
}
