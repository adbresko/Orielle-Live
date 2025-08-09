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
     * Trigger manual sync of all pending data
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
