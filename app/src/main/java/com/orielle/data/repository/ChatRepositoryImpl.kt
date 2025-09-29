package com.orielle.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.orielle.data.local.dao.ChatConversationDao
import com.orielle.data.local.dao.ChatMessageDao
import com.orielle.data.local.dao.TagDao
import com.orielle.data.local.model.TagEntity
import com.orielle.data.local.model.ConversationTagCrossRef
import com.orielle.data.mapper.toDomain
import com.orielle.data.mapper.toEntity
import com.orielle.domain.manager.SessionManager
import com.orielle.domain.model.AppError
import com.orielle.domain.model.ChatConversation
import com.orielle.domain.model.ChatMessage
import com.orielle.domain.model.Response
import com.orielle.domain.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.*
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val conversationDao: ChatConversationDao,
    private val messageDao: ChatMessageDao,
    private val tagDao: TagDao,
    private val firestore: FirebaseFirestore,
    private val sessionManager: SessionManager
) : ChatRepository {

    override suspend fun saveConversation(conversation: ChatConversation): Response<String> {
        return try {
            val userId = sessionManager.currentUserId.first()
                ?: return Response.Failure(AppError.Auth, Exception("No user session found."))
            val isGuest = sessionManager.isGuest.first()

            val conversationToSave = conversation.copy(
                userId = userId,
                id = if (conversation.id.isBlank()) UUID.randomUUID().toString() else conversation.id
            )

            // Save to local database first
            conversationDao.upsertConversation(conversationToSave.toEntity())

            // Sync to Firebase if not a guest user
            if (!isGuest) {
                firestore.collection("users").document(userId)
                    .collection("conversations").document(conversationToSave.id)
                    .set(conversationToSave).await()
            }

            Response.Success(conversationToSave.id)
        } catch (e: Exception) {
            Timber.e(e, "Error saving conversation")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    override fun getConversationsForUser(): Flow<Response<List<ChatConversation>>> {
        return sessionManager.currentUserId.map { userId ->
            if (userId == null) {
                Response.Success(emptyList<ChatConversation>())
            } else {
                try {
                    conversationDao.getConversationsForUser(userId).map { entities ->
                        Response.Success(entities.map { it.toDomain() })
                    }.first()
                } catch (e: Exception) {
                    Timber.e(e, "Error fetching conversations")
                    FirebaseCrashlytics.getInstance().recordException(e)
                    Response.Failure(AppError.Database, e)
                }
            }
        }
    }

    override suspend fun getConversationById(conversationId: String): Response<ChatConversation?> {
        return try {
            val entity = conversationDao.getConversationById(conversationId)
            Response.Success(entity?.toDomain())
        } catch (e: Exception) {
            Timber.e(e, "Error fetching conversation by ID")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    override suspend fun deleteConversation(conversationId: String): Response<Unit> {
        return try {
            val userId = sessionManager.currentUserId.first()
                ?: return Response.Failure(AppError.Auth, Exception("No user session found."))
            val isGuest = sessionManager.isGuest.first()

            // Delete from local database
            conversationDao.deleteConversation(conversationId)

            // Delete from Firebase if not a guest user
            if (!isGuest) {
                firestore.collection("users").document(userId)
                    .collection("conversations").document(conversationId)
                    .delete().await()
            }

            Response.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting conversation")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    override suspend fun updateConversationSavedStatus(conversationId: String, isSaved: Boolean): Response<Unit> {
        return try {
            conversationDao.updateSavedStatus(conversationId, isSaved)
            Response.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating conversation saved status")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    override suspend fun updateConversationTitleAndTags(conversationId: String, title: String, tags: List<String>): Response<Unit> {
        return try {
            val userId = sessionManager.currentUserId.first()
                ?: return Response.Failure(AppError.Auth, Exception("No user session found."))

            // Update conversation title
            conversationDao.updateTitle(conversationId, title)

            // Remove all existing tags for this conversation
            tagDao.removeAllTagsFromConversation(conversationId)

            // Add new tags
            for (tagName in tags) {
                // Get or create tag
                var tag = tagDao.getTagByName(tagName, userId)
                if (tag == null) {
                    // Create new tag
                    val newTag = TagEntity(
                        id = UUID.randomUUID().toString(),
                        name = tagName,
                        userId = userId,
                        isUserCreated = true,
                        usageCount = 0,
                        createdAt = java.util.Date()
                    )
                    tagDao.upsertTag(newTag)
                    tag = newTag
                } else {
                    // Increment usage count for existing tag
                    tagDao.incrementTagUsage(tag.id)
                }

                // Create conversation-tag relationship
                val crossRef = ConversationTagCrossRef(
                    conversationId = conversationId,
                    tagId = tag.id
                )
                tagDao.insertConversationTagCrossRef(crossRef)
            }

            Timber.d("Updated conversation $conversationId with title: '$title' and tags: $tags")
            Response.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating conversation title and tags")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    override suspend fun saveMessage(message: ChatMessage): Response<String> {
        return try {
            val userId = sessionManager.currentUserId.first()
                ?: return Response.Failure(AppError.Auth, Exception("No user session found."))
            val isGuest = sessionManager.isGuest.first()

            val messageToSave = message.copy(
                id = if (message.id.isBlank()) UUID.randomUUID().toString() else message.id
            )

            // Save to local database first
            messageDao.upsertMessage(messageToSave.toEntity())

            // Update conversation stats
            updateConversationStats(message.conversationId)

            // Sync to Firebase if not a guest user
            if (!isGuest) {
                firestore.collection("users").document(userId)
                    .collection("conversations").document(message.conversationId)
                    .collection("messages").document(messageToSave.id)
                    .set(messageToSave).await()
            }

            Response.Success(messageToSave.id)
        } catch (e: Exception) {
            Timber.e(e, "Error saving message")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    override fun getMessagesForConversation(conversationId: String): Flow<Response<List<ChatMessage>>> {
        return messageDao.getMessagesForConversation(conversationId).map { entities ->
            try {
                Response.Success(entities.map { it.toDomain() })
            } catch (e: Exception) {
                Timber.e(e, "Error fetching messages for conversation")
                FirebaseCrashlytics.getInstance().recordException(e)
                Response.Failure(AppError.Database, e)
            }
        }
    }

    override suspend fun deleteMessage(messageId: String): Response<Unit> {
        return try {
            val userId = sessionManager.currentUserId.first()
                ?: return Response.Failure(AppError.Auth, Exception("No user session found."))
            val isGuest = sessionManager.isGuest.first()

            // Get message details before deletion for Firebase cleanup
            val message = messageDao.getMessageById(messageId)

            // Delete from local database
            messageDao.deleteMessage(messageId)

            // Delete from Firebase if not a guest user
            if (!isGuest && message != null) {
                firestore.collection("users").document(userId)
                    .collection("conversations").document(message.conversationId)
                    .collection("messages").document(messageId)
                    .delete().await()
            }

            Response.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting message")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    override suspend fun deleteAllMessagesInConversation(conversationId: String): Response<Unit> {
        return try {
            messageDao.deleteAllMessagesInConversation(conversationId)
            Response.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting all messages in conversation")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    override suspend fun updateConversationStats(conversationId: String): Response<Unit> {
        return try {
            val messageCount = messageDao.getMessageCount(conversationId)
            val latestMessage = messageDao.getLatestMessage(conversationId)
            val preview = latestMessage?.content?.take(100) // First 100 chars as preview

            conversationDao.updateConversationStats(
                conversationId = conversationId,
                count = messageCount,
                updatedAt = Date(),
                preview = preview
            )

            Response.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating conversation stats")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }
}
