package com.orielle.data.manager

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.orielle.data.local.dao.*
import com.orielle.data.mapper.*
import com.orielle.domain.manager.SessionManager
import com.orielle.domain.manager.SyncManager
import com.orielle.util.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkUtils: NetworkUtils,
    private val sessionManager: SessionManager,
    private val firestore: FirebaseFirestore,
    private val journalDao: JournalDao,
    private val moodCheckInDao: MoodCheckInDao,
    private val conversationDao: ChatConversationDao,
    private val messageDao: ChatMessageDao,
    private val tagDao: TagDao
) : SyncManager {

    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    override val isNetworkAvailable: Flow<Boolean> = networkUtils.observeNetworkConnectivity()

    override suspend fun isCurrentlyOnline(): Boolean {
        return networkUtils.isNetworkAvailable()
    }

    override suspend fun syncPendingData(): Result<Unit> {
        if (!isCurrentlyOnline()) {
            return Result.failure(Exception("No network connection available"))
        }

        val userId = sessionManager.currentUserId.first()
        if (userId == null) {
            return Result.failure(Exception("No user session found"))
        }

        val isGuest = sessionManager.isGuest.first()
        if (isGuest) {
            // Guest users don't sync to cloud
            return Result.success(Unit)
        }

        _isSyncing.value = true

        return try {
            // Sync all data types
            syncMoodCheckIns()
            syncJournalEntries()
            syncConversations()
            syncTags()

            Timber.d("All data synchronized successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error during data synchronization")
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
    }

    override suspend fun syncMoodCheckIns(): Result<Unit> {
        return try {
            val userId = sessionManager.currentUserId.first() ?: return Result.failure(Exception("No user"))

            // Get all local mood check-ins
            val localCheckIns = moodCheckInDao.getMoodCheckInsByUserId(userId).first()

            // Upload each mood check-in to Firebase
            localCheckIns.forEach { entity ->
                val domain = entity.toMoodCheckIn()
                firestore.collection("users").document(userId)
                    .collection("mood_check_ins").document(domain.id)
                    .set(domain).await()
            }

            Timber.d("Mood check-ins synchronized: ${localCheckIns.size} items")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing mood check-ins")
            Result.failure(e)
        }
    }

    override suspend fun syncJournalEntries(): Result<Unit> {
        return try {
            val userId = sessionManager.currentUserId.first() ?: return Result.failure(Exception("No user"))

            // Get all local journal entries
            val localEntries = journalDao.getJournalEntries(userId).first()

            // Upload each journal entry to Firebase
            localEntries.forEach { entity ->
                val domain = entity.toDomain()
                firestore.collection("users").document(userId)
                    .collection("journal_entries").document(domain.id)
                    .set(domain).await()
            }

            Timber.d("Journal entries synchronized: ${localEntries.size} items")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing journal entries")
            Result.failure(e)
        }
    }

    override suspend fun syncConversations(): Result<Unit> {
        return try {
            val userId = sessionManager.currentUserId.first() ?: return Result.failure(Exception("No user"))

            // Get all local conversations
            val localConversations = conversationDao.getConversationsForUser(userId).first()

            // Upload each conversation and its messages to Firebase
            localConversations.forEach { conversationEntity ->
                val conversation = conversationEntity.toDomain()

                // Upload conversation
                firestore.collection("users").document(userId)
                    .collection("conversations").document(conversation.id)
                    .set(conversation).await()

                // Upload all messages for this conversation
                val messages = messageDao.getMessagesForConversation(conversation.id).first()
                messages.forEach { messageEntity ->
                    val message = messageEntity.toDomain()
                    firestore.collection("users").document(userId)
                        .collection("conversations").document(conversation.id)
                        .collection("messages").document(message.id)
                        .set(message).await()
                }
            }

            Timber.d("Conversations synchronized: ${localConversations.size} items")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing conversations")
            Result.failure(e)
        }
    }

    override suspend fun syncTags(): Result<Unit> {
        return try {
            val userId = sessionManager.currentUserId.first() ?: return Result.failure(Exception("No user"))

            // Get all local tags
            val localTags = tagDao.getTagsForUser(userId).first()

            // Upload each tag to Firebase
            localTags.forEach { entity ->
                val domain = entity.toDomain()
                firestore.collection("users").document(userId)
                    .collection("tags").document(domain.id)
                    .set(domain).await()
            }

            Timber.d("Tags synchronized: ${localTags.size} items")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error syncing tags")
            Result.failure(e)
        }
    }
}
