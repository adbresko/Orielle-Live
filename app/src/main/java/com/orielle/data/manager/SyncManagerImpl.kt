package com.orielle.data.manager

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.orielle.data.local.dao.ChatConversationDao
import com.orielle.data.local.dao.ChatMessageDao
import com.orielle.data.local.dao.JournalDao
import com.orielle.data.local.dao.MoodCheckInDao
import com.orielle.data.local.dao.TagDao
import com.orielle.data.mapper.toEntity
import com.orielle.data.mapper.toMoodCheckIn
import com.orielle.data.mapper.toMoodCheckInEntity
import com.orielle.data.mapper.toDomain
import com.orielle.domain.manager.SessionManager
import com.orielle.domain.manager.SyncManager
import com.orielle.domain.model.MoodCheckIn
import com.orielle.util.NetworkUtils
import com.google.firebase.Timestamp
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

    override suspend fun downloadCloudData(): Result<Unit> {
        if (!isCurrentlyOnline()) {
            return Result.failure(Exception("No network connection available"))
        }

        val userId = sessionManager.currentUserId.first()
        if (userId == null) {
            return Result.failure(Exception("No user session found"))
        }

        val isGuest = sessionManager.isGuest.first()
        if (isGuest) {
            return Result.success(Unit)
        }

        _isSyncing.value = true

        return try {
            Timber.d("🔄 Starting cloud data download for user: $userId")

            // Download all data types from cloud to local
            downloadMoodCheckIns()
            downloadJournalEntries()
            downloadConversations()
            downloadTags()

            Timber.d("✅ All cloud data downloaded successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to download cloud data")
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
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
            Timber.d("🔄 Starting pending data sync (local → cloud)")
            // Sync all data types (upload local changes to cloud)
            syncMoodCheckIns()
            syncJournalEntries()
            syncConversations()
            syncTags()

            Timber.d("✅ All data synchronized successfully")
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

    private suspend fun downloadMoodCheckIns() {
        val userId = sessionManager.currentUserId.first() ?: return
        try {
            Timber.d("⬇️ Downloading mood check-ins from cloud")
            val snapshot = firestore.collection("users").document(userId)
                .collection("mood_check_ins").get().await()

            // Get existing local mood check-ins to avoid duplicates
            val localMoods = moodCheckInDao.getMoodCheckInsByUserId(userId).first()
            val localMoodIds = localMoods.map { it.id }.toSet()

            var newFromCloud = 0
            for (doc in snapshot.documents) {
                try {
                    val data = doc.data
                    if (data != null && doc.id !in localMoodIds) {
                        // Create MoodCheckIn from Firestore data
                        val cloudMood = com.orielle.domain.model.MoodCheckIn(
                            id = doc.id,
                            userId = data["userId"] as? String ?: "",
                            mood = data["mood"] as? String ?: "",
                            tags = (data["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                            timestamp = (data["timestamp"] as? Timestamp)?.toDate() ?: java.util.Date(),
                            notes = data["notes"] as? String
                        )

                        // Only insert if not already in local database
                        moodCheckInDao.insertMoodCheckIn(cloudMood.toMoodCheckInEntity())
                        newFromCloud++
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Failed to deserialize mood check-in: ${doc.id}")
                }
            }
            Timber.d("✅ Mood check-ins: ${snapshot.size()} from cloud, $newFromCloud new items added, ${localMoods.size} existing local items preserved")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to download mood check-ins")
            throw e
        }
    }

    private suspend fun downloadJournalEntries() {
        val userId = sessionManager.currentUserId.first() ?: return
        try {
            Timber.d("⬇️ Downloading journal entries from cloud")
            val snapshot = firestore.collection("users").document(userId)
                .collection("journal_entries").get().await()

            // Get existing local journal entries to avoid duplicates
            val localEntries = journalDao.getJournalEntries(userId).first()
            val localEntryIds = localEntries.map { it.id }.toSet()

            var newFromCloud = 0
            for (doc in snapshot.documents) {
                val cloudEntry = doc.toObject<com.orielle.domain.model.JournalEntry>(com.orielle.domain.model.JournalEntry::class.java)
                if (cloudEntry != null && cloudEntry.id !in localEntryIds) {
                    // Only insert if not already in local database
                    journalDao.upsertJournalEntry(cloudEntry.toEntity())
                    newFromCloud++
                }
            }
            Timber.d("✅ Journal entries: ${snapshot.size()} from cloud, $newFromCloud new items added, ${localEntries.size} existing local items preserved")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to download journal entries")
            throw e
        }
    }

    private suspend fun downloadConversations() {
        val userId = sessionManager.currentUserId.first() ?: return
        try {
            Timber.d("⬇️ Downloading conversations from cloud")
            val snapshot = firestore.collection("users").document(userId)
                .collection("conversations").get().await()

            // Get existing local conversations to avoid duplicates
            val localConversations = conversationDao.getConversationsForUser(userId).first()
            val localConversationIds = localConversations.map { it.id }.toSet()

            var newConversationsFromCloud = 0
            var newMessagesFromCloud = 0

            for (doc in snapshot.documents) {
                val cloudConversation = doc.toObject<com.orielle.domain.model.ChatConversation>(com.orielle.domain.model.ChatConversation::class.java)
                if (cloudConversation != null && cloudConversation.id !in localConversationIds) {
                    // Save conversation to local database
                    conversationDao.upsertConversation(cloudConversation.toEntity())
                    newConversationsFromCloud++

                    // Download messages for this conversation
                    val messagesSnapshot = doc.reference.collection("messages").get().await()
                    for (messageDoc in messagesSnapshot.documents) {
                        val cloudMessage = messageDoc.toObject<com.orielle.domain.model.ChatMessage>(com.orielle.domain.model.ChatMessage::class.java)
                        if (cloudMessage != null) {
                            messageDao.upsertMessage(cloudMessage.toEntity())
                            newMessagesFromCloud++
                        }
                    }
                }
            }
            Timber.d("✅ Conversations: ${snapshot.size()} from cloud, $newConversationsFromCloud new conversations added, $newMessagesFromCloud new messages added, ${localConversations.size} existing local conversations preserved")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to download conversations")
            throw e
        }
    }

    private suspend fun downloadTags() {
        val userId = sessionManager.currentUserId.first() ?: return
        try {
            Timber.d("⬇️ Downloading tags from cloud")
            val snapshot = firestore.collection("users").document(userId)
                .collection("tags").get().await()

            for (doc in snapshot.documents) {
                val cloudTag = doc.toObject<com.orielle.data.local.model.TagEntity>(com.orielle.data.local.model.TagEntity::class.java)
                if (cloudTag != null) {
                    // Save to local database
                    tagDao.upsertTag(cloudTag)
                }
            }
            Timber.d("✅ Tags downloaded: ${snapshot.size()} items")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to download tags")
            throw e
        }
    }
}
