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
}