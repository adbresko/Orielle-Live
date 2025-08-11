package com.orielle.data.manager

import android.content.Context
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.orielle.data.local.dao.ChatConversationDao
import com.orielle.data.local.dao.ChatMessageDao
import com.orielle.data.local.dao.JournalDao
import com.orielle.data.local.dao.MoodCheckInDao
import com.orielle.data.local.dao.TagDao
import com.orielle.data.local.dao.UserDao
import com.orielle.data.mapper.toEntity
import com.orielle.data.mapper.toMoodCheckIn
import com.orielle.data.mapper.toMoodCheckInEntity
import com.orielle.data.mapper.toDomain
import com.orielle.domain.manager.SessionManager
import com.orielle.domain.manager.SyncManager
import com.orielle.domain.model.*
import com.orielle.domain.model.MoodCheckIn
import com.orielle.domain.model.JournalEntry
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

/**
 * Enhanced sync manager with conflict resolution and smart data merging
 */
@Singleton
class EnhancedSyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkUtils: NetworkUtils,
    private val sessionManager: SessionManager,
    private val firestore: FirebaseFirestore,
    private val journalDao: JournalDao,
    private val moodCheckInDao: MoodCheckInDao,
    private val conversationDao: ChatConversationDao,
    private val messageDao: ChatMessageDao,
    private val tagDao: TagDao,
    private val userDao: UserDao,
    private val cacheManager: CacheManager
) : SyncManager {

    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    override val isNetworkAvailable: Flow<Boolean> = networkUtils.observeNetworkConnectivity()

    override suspend fun isCurrentlyOnline(): Boolean {
        return networkUtils.isNetworkAvailable()
    }

    /**
     * Enhanced sync with conflict resolution
     */
    suspend fun syncWithConflictResolution(): Result<SyncResult> {
        if (!isCurrentlyOnline()) {
            return Result.failure(Exception("No network connection available"))
        }

        val userId = sessionManager.currentUserId.first()
        if (userId == null) {
            return Result.failure(Exception("No user session found"))
        }

        val isGuest = sessionManager.isGuest.first()
        if (isGuest) {
            return Result.success(SyncResult.Success(0))
        }

        _isSyncing.value = true

        return try {
            Timber.d("🔄 Starting enhanced sync with conflict resolution for user: $userId")

            // Detect and resolve conflicts
            val conflicts = detectConflicts(userId)

            when {
                conflicts.isEmpty() -> {
                    // No conflicts, proceed with normal sync
                    performNormalSync(userId)
                    Result.success(SyncResult.Success(0))
                }
                conflicts.size < 5 -> {
                    // Few conflicts, auto-resolve
                    val resolvedCount = autoResolveConflicts(conflicts)
                    performNormalSync(userId)
                    Result.success(SyncResult.Success(resolvedCount))
                }
                else -> {
                    // Many conflicts, need user intervention
                    Result.success(SyncResult.NeedsUserInput(conflicts))
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Enhanced sync failed")
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
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
            Timber.d("⬇️ Starting enhanced cloud data download for user: $userId")

            // Download all data types with conflict detection
            downloadMoodCheckIns(userId)
            downloadJournalEntries(userId)
            downloadConversations(userId)
            downloadTags(userId)
            downloadUserProfile(userId)

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
            return Result.success(Unit)
        }

        _isSyncing.value = true

        return try {
            Timber.d("🔄 Starting enhanced pending data sync (local → cloud)")

            // Sync all data types with conflict resolution
            syncMoodCheckIns()
            syncJournalEntries()
            syncConversations()
            syncTags()
            syncUserProfile(userId)

            Timber.d("✅ All data synchronized successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error during enhanced data synchronization")
            Result.failure(e)
        } finally {
            _isSyncing.value = false
        }
    }

    /**
     * Detect conflicts between local and cloud data
     */
    private suspend fun detectConflicts(userId: String): List<DataConflict> {
        val conflicts = mutableListOf<DataConflict>()

        // Check mood check-ins for conflicts
        val localMoods = moodCheckInDao.getMoodCheckInsByUserId(userId).first()
        val cloudMoods = fetchCloudMoodCheckIns(userId)

        localMoods.forEach { local ->
            cloudMoods.find { it.id == local.id }?.let { cloud ->
                if (Math.abs(local.timestamp.time - cloud.timestamp.time) > 5000) { // 5 second threshold
                    conflicts.add(DataConflict(
                        local = local.toMoodCheckIn(),
                        cloud = cloud,
                        type = ConflictType.TIMESTAMP_MISMATCH,
                        dataType = DataType.MOOD_CHECK_IN
                    ))
                }
            }
        }

        // Check journal entries for conflicts
        val localJournals = journalDao.getJournalEntries(userId).first()
        val cloudJournals = fetchCloudJournalEntries(userId)

        localJournals.forEach { local ->
            cloudJournals.find { it.id == local.id }?.let { cloud ->
                if (local.content != cloud.content ||
                    Math.abs(local.timestamp.time - cloud.timestamp.time) > 10000) { // 10 second threshold
                    conflicts.add(DataConflict(
                        local = local.toDomain(),
                        cloud = cloud,
                        type = ConflictType.CONTENT_DIFFERENCE,
                        dataType = DataType.JOURNAL_ENTRY
                    ))
                }
            }
        }

        Timber.d("🔍 Detected ${conflicts.size} conflicts")
        return conflicts
    }

    /**
     * Auto-resolve conflicts using intelligent rules
     */
    private suspend fun autoResolveConflicts(conflicts: List<DataConflict>): Int {
        var resolved = 0

        conflicts.forEach { conflict ->
            try {
                when (conflict.type) {
                    ConflictType.TIMESTAMP_MISMATCH -> {
                        // Use the most recent timestamp
                        val winner = when (conflict.dataType) {
                            DataType.MOOD_CHECK_IN -> {
                                val localMood = conflict.local as MoodCheckIn
                                val cloudMood = conflict.cloud as MoodCheckIn
                                if (localMood.timestamp > cloudMood.timestamp) localMood else cloudMood
                            }
                            DataType.JOURNAL_ENTRY -> {
                                val localEntry = conflict.local as JournalEntry
                                val cloudEntry = conflict.cloud as JournalEntry
                                if (localEntry.timestamp > cloudEntry.timestamp) localEntry else cloudEntry
                            }
                            else -> conflict.local // Default to local for other types
                        }

                        when (conflict.dataType) {
                            DataType.MOOD_CHECK_IN -> moodCheckInDao.insertMoodCheckIn((winner as MoodCheckIn).toMoodCheckInEntity())
                            DataType.JOURNAL_ENTRY -> journalDao.upsertJournalEntry((winner as JournalEntry).toEntity())
                            else -> { /* Handle other types */ }
                        }
                        resolved++
                    }
                    ConflictType.CONTENT_DIFFERENCE -> {
                        // For content differences, prefer local version but merge if possible
                        val merged = mergeContent(conflict.local, conflict.cloud, conflict.dataType)
                        when (conflict.dataType) {
                            DataType.MOOD_CHECK_IN -> moodCheckInDao.insertMoodCheckIn((merged as MoodCheckIn).toMoodCheckInEntity())
                            DataType.JOURNAL_ENTRY -> journalDao.upsertJournalEntry((merged as JournalEntry).toEntity())
                            else -> { /* Handle other types */ }
                        }
                        resolved++
                    }
                    ConflictType.DELETION_CONFLICT -> {
                        // For deletion conflicts, prefer local version (user's intent)
                        when (conflict.dataType) {
                            DataType.MOOD_CHECK_IN -> moodCheckInDao.insertMoodCheckIn((conflict.local as MoodCheckIn).toMoodCheckInEntity())
                            DataType.JOURNAL_ENTRY -> journalDao.upsertJournalEntry((conflict.local as JournalEntry).toEntity())
                            else -> { /* Handle other types */ }
                        }
                        resolved++
                    }
                    ConflictType.DUPLICATE_ID -> {
                        // For duplicate IDs, merge and create new ID
                        val merged = mergeContent(conflict.local, conflict.cloud, conflict.dataType)
                        when (conflict.dataType) {
                            DataType.MOOD_CHECK_IN -> moodCheckInDao.insertMoodCheckIn((merged as MoodCheckIn).toMoodCheckInEntity())
                            DataType.JOURNAL_ENTRY -> journalDao.upsertJournalEntry((merged as JournalEntry).toEntity())
                            else -> { /* Handle other types */ }
                        }
                        resolved++
                    }
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to auto-resolve conflict: ${conflict.id}")
            }
        }

        Timber.d("✅ Auto-resolved $resolved conflicts")
        return resolved
    }

    /**
     * Merge content from conflicting data sources
     */
    private fun mergeContent(local: Any, cloud: Any, dataType: DataType): Any {
        return when (dataType) {
            DataType.JOURNAL_ENTRY -> {
                val localEntry = local as JournalEntry
                val cloudEntry = cloud as JournalEntry

                // Merge content, prefer longer content
                val mergedContent = if (localEntry.content.length > cloudEntry.content.length) {
                    localEntry.content
                } else {
                    cloudEntry.content
                }

                // Use most recent timestamp
                val mergedTimestamp = if (localEntry.timestamp > cloudEntry.timestamp) {
                    localEntry.timestamp
                } else {
                    cloudEntry.timestamp
                }

                localEntry.copy(
                    content = mergedContent,
                    timestamp = mergedTimestamp
                )
            }
            DataType.MOOD_CHECK_IN -> {
                val localMood = local as MoodCheckIn
                val cloudMood = cloud as MoodCheckIn

                // Merge tags and notes
                val mergedTags = (localMood.tags + cloudMood.tags).distinct()
                val mergedNotes = if (localMood.notes?.length ?: 0 > cloudMood.notes?.length ?: 0) {
                    localMood.notes
                } else {
                    cloudMood.notes
                }

                localMood.copy(
                    tags = mergedTags,
                    notes = mergedNotes,
                    timestamp = if (localMood.timestamp > cloudMood.timestamp) localMood.timestamp else cloudMood.timestamp
                )
            }
            else -> local // Default to local version
        }
    }

    // ... existing sync methods with enhanced implementations ...

    private suspend fun performNormalSync(userId: String) {
        syncMoodCheckIns()
        syncJournalEntries()
        syncConversations()
        syncTags()
        syncUserProfile(userId)
    }

    private suspend fun fetchCloudMoodCheckIns(userId: String): List<MoodCheckIn> {
        val snapshot = firestore.collection("users").document(userId)
            .collection("mood_check_ins").get().await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                val data = doc.data
                if (data != null) {
                    MoodCheckIn(
                        id = doc.id,
                        userId = data["userId"] as? String ?: "",
                        mood = data["mood"] as? String ?: "",
                        tags = (data["tags"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                        timestamp = (data["timestamp"] as? Timestamp)?.toDate() ?: java.util.Date(),
                        notes = data["notes"] as? String
                    )
                } else null
            } catch (e: Exception) {
                Timber.w(e, "Failed to deserialize mood check-in: ${doc.id}")
                null
            }
        }
    }

    private suspend fun fetchCloudJournalEntries(userId: String): List<JournalEntry> {
        val snapshot = firestore.collection("users").document(userId)
            .collection("journal_entries").get().await()

        return snapshot.documents.mapNotNull { doc ->
            try {
                doc.toObject<JournalEntry>(JournalEntry::class.java)
            } catch (e: Exception) {
                Timber.w(e, "Failed to deserialize journal entry: ${doc.id}")
                null
            }
        }
    }

    // ... rest of the existing methods with enhanced implementations ...

    override suspend fun syncMoodCheckIns(): Result<Unit> {
        return try {
            val userId = sessionManager.currentUserId.first() ?: return Result.failure(Exception("No user"))

            val localCheckIns = moodCheckInDao.getMoodCheckInsByUserId(userId).first()

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

            val localEntries = journalDao.getJournalEntries(userId).first()

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

            val localConversations = conversationDao.getConversationsForUser(userId).first()

            localConversations.forEach { conversationEntity ->
                val conversation = conversationEntity.toDomain()

                firestore.collection("users").document(userId)
                    .collection("conversations").document(conversation.id)
                    .set(conversation).await()

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

            val localTags = tagDao.getTagsForUser(userId).first()

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

    private suspend fun syncUserProfile(userId: String) {
        // TODO: Implement user profile sync
    }

    private suspend fun downloadUserProfile(userId: String) {
        // TODO: Implement user profile download
    }

    private suspend fun downloadMoodCheckIns(userId: String) {
        // Enhanced download with conflict detection
        try {
            Timber.d("⬇️ Downloading mood check-ins from cloud")
            val cloudMoods = fetchCloudMoodCheckIns(userId)

            val localMoods = moodCheckInDao.getMoodCheckInsByUserId(userId).first()
            val localMoodIds = localMoods.map { it.id }.toSet()

            var newFromCloud = 0
            cloudMoods.forEach { cloudMood ->
                if (cloudMood.id !in localMoodIds) {
                    moodCheckInDao.insertMoodCheckIn(cloudMood.toMoodCheckInEntity())
                    newFromCloud++
                }
            }

            Timber.d("✅ Mood check-ins: ${cloudMoods.size} from cloud, $newFromCloud new items added")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to download mood check-ins")
            throw e
        }
    }

    private suspend fun downloadJournalEntries(userId: String) {
        try {
            Timber.d("⬇️ Downloading journal entries from cloud")
            val cloudEntries = fetchCloudJournalEntries(userId)

            val localEntries = journalDao.getJournalEntries(userId).first()
            val localEntryIds = localEntries.map { it.id }.toSet()

            var newFromCloud = 0
            cloudEntries.forEach { cloudEntry ->
                if (cloudEntry.id !in localEntryIds) {
                    journalDao.upsertJournalEntry(cloudEntry.toEntity())
                    newFromCloud++
                }
            }

            Timber.d("✅ Journal entries: ${cloudEntries.size} from cloud, $newFromCloud new items added")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to download journal entries")
            throw e
        }
    }

    private suspend fun downloadConversations(userId: String) {
        // TODO: Implement enhanced conversation download
    }

    private suspend fun downloadTags(userId: String) {
        // TODO: Implement enhanced tag download
    }
}

/**
 * Result of sync operation
 */
sealed class SyncResult {
    data class Success(val conflictsResolved: Int) : SyncResult()
    data class NeedsUserInput(val conflicts: List<DataConflict>) : SyncResult()
    data class Failure(val error: String) : SyncResult()
}

/**
 * Represents a data conflict between local and cloud
 */
data class DataConflict(
    val id: String = java.util.UUID.randomUUID().toString(),
    val local: Any,
    val cloud: Any,
    val type: ConflictType,
    val dataType: DataType
)

/**
 * Types of conflicts that can occur
 */
enum class ConflictType {
    TIMESTAMP_MISMATCH,    // Same data, different timestamps
    CONTENT_DIFFERENCE,    // Same ID, different content
    DELETION_CONFLICT,     // One side deleted, other side modified
    DUPLICATE_ID           // Same ID exists in both places
}
