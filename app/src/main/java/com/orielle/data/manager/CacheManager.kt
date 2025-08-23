package com.orielle.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.orielle.domain.model.DataType
import com.orielle.domain.model.UserActivityLevel
import com.orielle.domain.model.CacheMetadata
import com.orielle.util.NetworkUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

// DataStore setup for cache preferences
private val Context.cacheDataStore: DataStore<Preferences> by preferencesDataStore(name = "cache_preferences")

@Singleton
class CacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val networkUtils: NetworkUtils
) {

    private object CacheKeys {
        val LAST_USER_ACTIVITY = longPreferencesKey("last_user_activity")
        val USER_ACTIVITY_LEVEL = longPreferencesKey("user_activity_level")
        val LAST_CACHE_CLEANUP = longPreferencesKey("last_cache_cleanup")
        val CACHE_ENABLED = booleanPreferencesKey("cache_enabled")
    }

    /**
     * Get cache timeout based on data type and user activity
     */
    fun getCacheTimeout(dataType: DataType, userActivity: UserActivityLevel): Long {
        return when (dataType) {
            DataType.MOOD_CHECK_IN -> when (userActivity) {
                UserActivityLevel.ACTIVE -> 2 * 60 * 1000L      // 2 minutes for active users
                UserActivityLevel.INACTIVE -> 10 * 60 * 1000L    // 10 minutes for inactive users
                UserActivityLevel.BACKGROUND -> 30 * 60 * 1000L  // 30 minutes for background
            }
            DataType.JOURNAL_ENTRY -> when (userActivity) {
                UserActivityLevel.ACTIVE -> 5 * 60 * 1000L       // 5 minutes for active users
                UserActivityLevel.INACTIVE -> 15 * 60 * 1000L    // 15 minutes for inactive users
                UserActivityLevel.BACKGROUND -> 60 * 60 * 1000L  // 1 hour for background
            }
            DataType.CHAT_MESSAGE -> when (userActivity) {
                UserActivityLevel.ACTIVE -> 1 * 60 * 1000L       // 1 minute for active users
                UserActivityLevel.INACTIVE -> 5 * 60 * 1000L     // 5 minutes for inactive users
                UserActivityLevel.BACKGROUND -> 15 * 60 * 1000L  // 15 minutes for background
            }
            DataType.USER_PROFILE -> when (userActivity) {
                UserActivityLevel.ACTIVE -> 10 * 60 * 1000L      // 10 minutes for active users
                UserActivityLevel.INACTIVE -> 30 * 60 * 1000L    // 30 minutes for inactive users
                UserActivityLevel.BACKGROUND -> 2 * 60 * 60 * 1000L // 2 hours for background
            }
            DataType.TAG -> when (userActivity) {
                UserActivityLevel.ACTIVE -> 15 * 60 * 1000L      // 15 minutes for active users
                UserActivityLevel.INACTIVE -> 60 * 60 * 1000L    // 1 hour for inactive users
                UserActivityLevel.BACKGROUND -> 4 * 60 * 60 * 1000L // 4 hours for background
            }
            DataType.CHAT_CONVERSATION -> when (userActivity) {
                UserActivityLevel.ACTIVE -> 2 * 60 * 1000L       // 2 minutes for active users
                UserActivityLevel.INACTIVE -> 10 * 60 * 1000L    // 10 minutes for inactive users
                UserActivityLevel.BACKGROUND -> 30 * 60 * 1000L  // 30 minutes for background
            }
        }
    }

    /**
     * Check if cache should be invalidated for given data
     */
    suspend fun shouldInvalidateCache(
        dataType: DataType,
        lastUpdate: Long
    ): Boolean {
        val userActivity = getUserActivityLevel()
        if (!networkUtils.isNetworkAvailable()) {
            Timber.d("Cache invalidation skipped - no network")
            return false
        }

        val cacheAge = System.currentTimeMillis() - lastUpdate
        val timeout = getCacheTimeout(dataType, userActivity)

        val shouldInvalidate = cacheAge > timeout ||
                hasHighPriorityUpdate(dataType) ||
                userExplicitlyRequestedRefresh()

        Timber.d("Cache invalidation check for $dataType: age=${cacheAge}ms, timeout=${timeout}ms, invalidate=$shouldInvalidate")

        return shouldInvalidate
    }

    /**
     * Check if data is stale based on cache metadata
     */
    suspend fun isDataStale(cacheMetadata: CacheMetadata, dataType: DataType): Boolean {
        val userActivity = getUserActivityLevel()
        val timeout = getCacheTimeout(dataType, userActivity)
        val age = System.currentTimeMillis() - cacheMetadata.lastUpdated

        return age > timeout
    }

    /**
     * Update user activity level
     */
    suspend fun updateUserActivity() {
        context.cacheDataStore.edit { preferences ->
            preferences[CacheKeys.LAST_USER_ACTIVITY] = System.currentTimeMillis()
            preferences[CacheKeys.USER_ACTIVITY_LEVEL] = UserActivityLevel.ACTIVE.ordinal.toLong()
        }
    }

    /**
     * Get current user activity level
     */
    private suspend fun getUserActivityLevel(): UserActivityLevel {
        val lastActivity = context.cacheDataStore.data.map { preferences ->
            preferences[CacheKeys.LAST_USER_ACTIVITY] ?: 0L
        }.first()

        val timeSinceLastActivity = System.currentTimeMillis() - lastActivity

        return when {
            timeSinceLastActivity < 5 * 60 * 1000L -> UserActivityLevel.ACTIVE      // 5 minutes
            timeSinceLastActivity < 30 * 60 * 1000L -> UserActivityLevel.INACTIVE   // 30 minutes
            else -> UserActivityLevel.BACKGROUND
        }
    }

    /**
     * Check if there are high priority updates that require immediate cache invalidation
     */
    private fun hasHighPriorityUpdate(dataType: DataType): Boolean {
        // High priority updates for critical data
        return when (dataType) {
            DataType.USER_PROFILE -> true  // Always check profile updates
            DataType.MOOD_CHECK_IN -> true // Mood data is time-sensitive
            else -> false
        }
    }

    /**
     * Check if user explicitly requested a refresh
     */
    private fun userExplicitlyRequestedRefresh(): Boolean {
        // This would be set by UI when user pulls to refresh or taps refresh button
        return false // TODO: Implement pull-to-refresh tracking
    }

    /**
     * Clean up old cache data
     */
    suspend fun cleanupOldCache() {
        val lastCleanup = context.cacheDataStore.data.map { preferences ->
            preferences[CacheKeys.LAST_CACHE_CLEANUP] ?: 0L
        }.first()

        val timeSinceLastCleanup = System.currentTimeMillis() - lastCleanup

        // Clean up cache every 24 hours
        if (timeSinceLastCleanup > 24 * 60 * 60 * 1000L) {
            Timber.d("Performing cache cleanup")
            // TODO: Implement cache cleanup logic
            context.cacheDataStore.edit { preferences ->
                preferences[CacheKeys.LAST_CACHE_CLEANUP] = System.currentTimeMillis()
            }
        }
    }

    /**
     * Enable/disable caching
     */
    suspend fun setCacheEnabled(enabled: Boolean) {
        context.cacheDataStore.edit { preferences ->
            preferences[CacheKeys.CACHE_ENABLED] = enabled
        }
    }

    /**
     * Check if caching is enabled
     */
    fun isCacheEnabled(): Flow<Boolean> {
        return context.cacheDataStore.data.map { preferences ->
            preferences[CacheKeys.CACHE_ENABLED] ?: true
        }
    }
}
