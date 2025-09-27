package com.orielle.data.cache

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages smart refresh strategies to avoid unnecessary data loading.
 * Implements TTL (Time To Live) caching and refresh throttling.
 */
@Singleton
class RefreshManager @Inject constructor() {
    
    private val lastRefreshTimes = ConcurrentHashMap<String, Long>()
    private val refreshMutex = Mutex()
    
    companion object {
        // Different TTL for different data types
        private const val PROFILE_TTL_MS = 1800000L // 30 minutes
        private const val MOOD_CHECK_TTL_MS = 300000L // 5 minutes
        private const val JOURNAL_TTL_MS = 600000L // 10 minutes
        private const val WEEKLY_MOOD_TTL_MS = 300000L // 5 minutes
        private const val QUOTES_TTL_MS = 3600000L // 1 hour
        
        // Minimum refresh interval to prevent excessive refreshes
        private const val MIN_REFRESH_INTERVAL_MS = 5000L // 5 seconds
    }
    
    /**
     * Checks if data needs refresh based on TTL and last refresh time.
     */
    suspend fun shouldRefresh(
        dataType: DataType,
        userId: String? = null,
        forceRefresh: Boolean = false
    ): Boolean {
        return refreshMutex.withLock {
            if (forceRefresh) return true
            
            val key = "${dataType.name}_${userId ?: "global"}"
            val lastRefresh = lastRefreshTimes[key] ?: 0L
            val now = System.currentTimeMillis()
            val ttl = getTtlForDataType(dataType)
            
            // Check if enough time has passed since last refresh
            val timeSinceLastRefresh = now - lastRefresh
            val needsRefresh = timeSinceLastRefresh > ttl
            
            // Also check minimum refresh interval to prevent excessive refreshes
            val canRefresh = timeSinceLastRefresh > MIN_REFRESH_INTERVAL_MS
            
            needsRefresh && canRefresh
        }
    }
    
    /**
     * Records that data was refreshed for a specific data type.
     */
    suspend fun recordRefresh(dataType: DataType, userId: String? = null) {
        refreshMutex.withLock {
            val key = "${dataType.name}_${userId ?: "global"}"
            lastRefreshTimes[key] = System.currentTimeMillis()
        }
    }
    
    /**
     * Gets the time since last refresh for a data type.
     */
    suspend fun getTimeSinceLastRefresh(dataType: DataType, userId: String? = null): Long {
        return refreshMutex.withLock {
            val key = "${dataType.name}_${userId ?: "global"}"
            val lastRefresh = lastRefreshTimes[key] ?: 0L
            System.currentTimeMillis() - lastRefresh
        }
    }
    
    /**
     * Clears refresh history for a specific user (useful on logout).
     */
    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun clearUserRefreshHistory(userId: String) {
        refreshMutex.withLock {
            lastRefreshTimes.entries.removeIf { (key, _) ->
                key.endsWith("_$userId")
            }
        }
    }
    
    /**
     * Clears all refresh history.
     */
    suspend fun clearAllRefreshHistory() {
        refreshMutex.withLock {
            lastRefreshTimes.clear()
        }
    }
    
    /**
     * Gets refresh statistics for debugging.
     */
    fun getRefreshStats(): Map<String, Any> {
        val now = System.currentTimeMillis()
        return mapOf(
            "totalDataTypes" to lastRefreshTimes.size,
            "refreshHistory" to lastRefreshTimes.mapValues { (_, timestamp) ->
                now - timestamp
            }
        )
    }
    
    private fun getTtlForDataType(dataType: DataType): Long {
        return when (dataType) {
            DataType.PROFILE -> PROFILE_TTL_MS
            DataType.MOOD_CHECK -> MOOD_CHECK_TTL_MS
            DataType.JOURNAL -> JOURNAL_TTL_MS
            DataType.WEEKLY_MOOD -> WEEKLY_MOOD_TTL_MS
            DataType.QUOTES -> QUOTES_TTL_MS
        }
    }
}

/**
 * Enum representing different types of data that can be refreshed.
 */
enum class DataType {
    PROFILE,
    MOOD_CHECK,
    JOURNAL,
    WEEKLY_MOOD,
    QUOTES
}
