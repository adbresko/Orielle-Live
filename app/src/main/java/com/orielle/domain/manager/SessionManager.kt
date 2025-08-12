package com.orielle.domain.manager

import kotlinx.coroutines.flow.Flow

/**
 * Defines the contract for managing user sessions, including both
 * authenticated Firebase users and local-only guest users.
 */
interface SessionManager {

    /**
     * A flow that emits the current user's unique ID.
     * This will be the Firebase UID for authenticated users or a
     * locally generated UUID for guests.
     */
    val currentUserId: Flow<String?>

    /**
     * A flow that emits whether the current user is a guest.
     */
    val isGuest: Flow<Boolean>

    /**
     * A flow that emits whether the current user has seen the onboarding.
     */
    val hasSeenOnboarding: Flow<Boolean>

    /**
     * Sets whether the current user has seen the onboarding.
     */
    suspend fun setHasSeenOnboarding(seen: Boolean)

    /**
     * Creates and persists a new, unique ID for a guest session.
     * This should only be called when the user explicitly chooses
     * the "Just Explore" path.
     */
    suspend fun startGuestSession()

    /**
     * Clears any persisted guest session data. This is typically
     * called after a guest successfully creates a permanent account
     * or when they explicitly log out.
     */
    suspend fun endGuestSession()

    suspend fun getLastCheckInTimestamp(): Long?
    suspend fun setLastCheckInTimestamp(timestamp: Long)
    suspend fun clearLastCheckInTimestamp()

    /**
     * Clears all session data including guest sessions, preferences, and check-in timestamps.
     * Used for account deletion and logout scenarios.
     */
    suspend fun clearSession()

    // --- NEW: User Profile Caching Methods ---

    /**
     * Caches user profile data to avoid repeated Firebase calls.
     * This data is stored locally and can be retrieved without network requests.
     */
    suspend fun cacheUserProfile(
        userId: String,
        firstName: String?,
        displayName: String?,
        email: String?,
        profileImageUrl: String?,
        isPremium: Boolean = false,
        notificationsEnabled: Boolean = true,
        twoFactorEnabled: Boolean = false
    )

    /**
     * Retrieves cached user profile data.
     * Returns null if no cached data exists or if the cache has expired.
     */
    suspend fun getCachedUserProfile(userId: String): CachedUserProfile?

    /**
     * Updates specific cached user profile fields.
     * Useful for partial updates without refetching the entire profile.
     */
    suspend fun updateCachedUserProfile(
        userId: String,
        firstName: String? = null,
        displayName: String? = null,
        email: String? = null,
        profileImageUrl: String? = null,
        isPremium: Boolean? = null,
        notificationsEnabled: Boolean? = null,
        twoFactorEnabled: Boolean? = null
    )

    /**
     * Clears cached user profile data for a specific user.
     * Called when user data is updated or when cache should be invalidated.
     */
    suspend fun clearCachedUserProfile(userId: String)

    /**
     * Checks if cached user profile data exists and is still valid.
     * Returns true if valid cached data exists, false otherwise.
     */
    suspend fun hasValidCachedProfile(userId: String): Boolean

    /**
     * Gets the cache expiration time in milliseconds.
     * Default is 1 hour (3600000 ms).
     */
    suspend fun getProfileCacheExpiration(): Long
}

/**
 * Data class for cached user profile information.
 * Contains all the commonly accessed user data fields.
 */
data class CachedUserProfile(
    val userId: String,
    val firstName: String?,
    val displayName: String?,
    val email: String?,
    val profileImageUrl: String?,
    val isPremium: Boolean,
    val notificationsEnabled: Boolean,
    val twoFactorEnabled: Boolean,
    val cachedAt: Long = System.currentTimeMillis()
)