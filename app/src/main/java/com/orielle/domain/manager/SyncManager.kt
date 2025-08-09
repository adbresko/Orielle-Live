package com.orielle.domain.manager

import kotlinx.coroutines.flow.Flow

/**
 * Manager interface for handling offline/online data synchronization.
 */
interface SyncManager {

    /**
     * Observe network connectivity status
     */
    val isNetworkAvailable: Flow<Boolean>

    /**
     * Check current network availability
     */
    suspend fun isCurrentlyOnline(): Boolean

    /**
     * Download all cloud data to local storage (cloud → local)
     * Used on sign-in to restore user data from Firebase
     */
    suspend fun downloadCloudData(): Result<Unit>

    /**
     * Upload pending local data to cloud (local → cloud)
     * Used to sync offline changes when network returns
     */
    suspend fun syncPendingData(): Result<Unit>

    /**
     * Sync specific data types
     */
    suspend fun syncMoodCheckIns(): Result<Unit>
    suspend fun syncJournalEntries(): Result<Unit>
    suspend fun syncConversations(): Result<Unit>
    suspend fun syncTags(): Result<Unit>

    /**
     * Handle sync state
     */
    val isSyncing: Flow<Boolean>
}
