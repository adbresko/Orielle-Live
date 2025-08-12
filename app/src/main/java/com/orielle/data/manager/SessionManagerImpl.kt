package com.orielle.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.orielle.domain.manager.SessionManager
import com.orielle.domain.manager.CachedUserProfile
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import com.google.gson.Gson
import timber.log.Timber

// DataStore setup - must be at top-level
internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "orielle_preferences")

@Singleton
class SessionManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
) : SessionManager {

    private object PreferencesKeys {
        val GUEST_UUID_KEY = stringPreferencesKey("guest_uuid")
        val LAST_CHECKIN_TIMESTAMP_KEY = stringPreferencesKey("last_checkin_timestamp")
        val ONBOARDING_SEEN_KEY = stringPreferencesKey("onboarding_seen")
        val BIOMETRICS_ENABLED_KEY = booleanPreferencesKey("biometrics_enabled")
        val PIN_CODE_KEY = stringPreferencesKey("pin_code")

        // --- NEW: Profile Caching Keys ---
        val CACHED_PROFILE_PREFIX = "cached_profile_"
        val PROFILE_CACHE_TIMESTAMP_PREFIX = "profile_cache_timestamp_"
        val PROFILE_CACHE_EXPIRATION = longPreferencesKey("profile_cache_expiration")
    }

    private val gson = Gson()
    private val defaultCacheExpiration = 3600000L // 1 hour in milliseconds

    override val currentUserId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            auth.currentUser?.uid ?: preferences[PreferencesKeys.GUEST_UUID_KEY]
        }

    override val isGuest: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            auth.currentUser == null && preferences[PreferencesKeys.GUEST_UUID_KEY] != null
        }

    override val hasSeenOnboarding: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_SEEN_KEY]?.toBoolean() ?: false
        }

    override suspend fun setHasSeenOnboarding(seen: Boolean) {
        context.dataStore.edit { preferences: MutablePreferences ->
            preferences[PreferencesKeys.ONBOARDING_SEEN_KEY] = seen.toString()
        }
    }

    override suspend fun startGuestSession() {
        context.dataStore.edit { preferences: MutablePreferences ->
            if (preferences[PreferencesKeys.GUEST_UUID_KEY] == null) {
                preferences[PreferencesKeys.GUEST_UUID_KEY] = UUID.randomUUID().toString()
            }
        }
    }

    override suspend fun endGuestSession() {
        context.dataStore.edit { preferences: MutablePreferences ->
            preferences.remove(PreferencesKeys.GUEST_UUID_KEY)
        }
    }

    override suspend fun setLastCheckInTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences: MutablePreferences ->
            preferences[PreferencesKeys.LAST_CHECKIN_TIMESTAMP_KEY] = timestamp.toString()
        }
    }

    override suspend fun getLastCheckInTimestamp(): Long? {
        val preferences = context.dataStore.data.first()
        val timestampString = preferences[PreferencesKeys.LAST_CHECKIN_TIMESTAMP_KEY]
        return timestampString?.toLongOrNull()
    }

    override suspend fun clearLastCheckInTimestamp() {
        context.dataStore.edit { preferences: MutablePreferences ->
            preferences.remove(PreferencesKeys.LAST_CHECKIN_TIMESTAMP_KEY)
        }
    }

    suspend fun setBiometricsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.BIOMETRICS_ENABLED_KEY] = enabled
        }
    }

    suspend fun isBiometricsEnabled(): Boolean {
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.BIOMETRICS_ENABLED_KEY] ?: false
    }

    suspend fun setPinCode(pin: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.PIN_CODE_KEY] = pin
        }
    }

    suspend fun getPinCode(): String? {
        val preferences = context.dataStore.data.first()
        return preferences[PreferencesKeys.PIN_CODE_KEY]
    }

    override suspend fun clearSession() {
        try {
            // Clear cached profile data for the current user if authenticated
            val currentUserId = auth.currentUser?.uid
            if (currentUserId != null) {
                clearCachedUserProfile(currentUserId)
                Timber.d("🗑️ Cleared cached profile data for user: $currentUserId")
            }

            // Clear all session data
            context.dataStore.edit { preferences ->
                preferences.clear()
            }

            Timber.d("✅ Session data cleared successfully")
        } catch (e: Exception) {
            Timber.e(e, "❌ Error clearing session data")
            // Try to clear basic preferences even if profile cache clearing fails
            context.dataStore.edit { preferences ->
                preferences.clear()
            }
        }
    }

    // --- NEW: Profile Caching Implementation ---

    override suspend fun cacheUserProfile(
        userId: String,
        firstName: String?,
        displayName: String?,
        email: String?,
        profileImageUrl: String?,
        isPremium: Boolean,
        notificationsEnabled: Boolean,
        twoFactorEnabled: Boolean
    ) {
        try {
            val cachedProfile = CachedUserProfile(
                userId = userId,
                firstName = firstName,
                displayName = displayName,
                email = email,
                profileImageUrl = profileImageUrl,
                isPremium = isPremium,
                notificationsEnabled = notificationsEnabled,
                twoFactorEnabled = twoFactorEnabled,
                cachedAt = System.currentTimeMillis()
            )

            val profileJson = gson.toJson(cachedProfile)
            val profileKey = stringPreferencesKey("${PreferencesKeys.CACHED_PROFILE_PREFIX}$userId")
            val timestampKey = longPreferencesKey("${PreferencesKeys.PROFILE_CACHE_TIMESTAMP_PREFIX}$userId")

            context.dataStore.edit { preferences ->
                preferences[profileKey] = profileJson
                preferences[timestampKey] = System.currentTimeMillis()
            }

            Timber.d("✅ Cached user profile for: $userId")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to cache user profile for: $userId")
        }
    }

    override suspend fun getCachedUserProfile(userId: String): CachedUserProfile? {
        return try {
            val preferences = context.dataStore.data.first()
            val profileKey = stringPreferencesKey("${PreferencesKeys.CACHED_PROFILE_PREFIX}$userId")
            val timestampKey = longPreferencesKey("${PreferencesKeys.PROFILE_CACHE_TIMESTAMP_PREFIX}$userId")

            val profileJson = preferences[profileKey] ?: return null
            val cacheTimestamp = preferences[timestampKey] ?: return null

            // Check if cache has expired
            val cacheExpiration = getProfileCacheExpiration()
            if (System.currentTimeMillis() - cacheTimestamp > cacheExpiration) {
                Timber.d("🕐 Cached profile expired for: $userId")
                clearCachedUserProfile(userId)
                return null
            }

            val cachedProfile = gson.fromJson(profileJson, CachedUserProfile::class.java)
            Timber.d("📋 Retrieved cached profile for: $userId")
            cachedProfile
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to retrieve cached profile for: $userId")
            null
        }
    }

    override suspend fun updateCachedUserProfile(
        userId: String,
        firstName: String?,
        displayName: String?,
        email: String?,
        profileImageUrl: String?,
        isPremium: Boolean?,
        notificationsEnabled: Boolean?,
        twoFactorEnabled: Boolean?
    ) {
        try {
            val existingProfile = getCachedUserProfile(userId)
            if (existingProfile != null) {
                val updatedProfile = existingProfile.copy(
                    firstName = firstName ?: existingProfile.firstName,
                    displayName = displayName ?: existingProfile.displayName,
                    email = email ?: existingProfile.email,
                    profileImageUrl = profileImageUrl ?: existingProfile.profileImageUrl,
                    isPremium = isPremium ?: existingProfile.isPremium,
                    notificationsEnabled = notificationsEnabled ?: existingProfile.notificationsEnabled,
                    twoFactorEnabled = twoFactorEnabled ?: existingProfile.twoFactorEnabled,
                    cachedAt = System.currentTimeMillis()
                )
                cacheUserProfile(
                    userId = updatedProfile.userId,
                    firstName = updatedProfile.firstName,
                    displayName = updatedProfile.displayName,
                    email = updatedProfile.email,
                    profileImageUrl = updatedProfile.profileImageUrl,
                    isPremium = updatedProfile.isPremium,
                    notificationsEnabled = updatedProfile.notificationsEnabled,
                    twoFactorEnabled = updatedProfile.twoFactorEnabled
                )
                Timber.d("🔄 Updated cached profile for: $userId")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to update cached profile for: $userId")
        }
    }

    override suspend fun clearCachedUserProfile(userId: String) {
        try {
            val profileKey = stringPreferencesKey("${PreferencesKeys.CACHED_PROFILE_PREFIX}$userId")
            val timestampKey = longPreferencesKey("${PreferencesKeys.PROFILE_CACHE_TIMESTAMP_PREFIX}$userId")

            context.dataStore.edit { preferences ->
                preferences.remove(profileKey)
                preferences.remove(timestampKey)
            }

            Timber.d("🗑️ Cleared cached profile for: $userId")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to clear cached profile for: $userId")
        }
    }

    override suspend fun hasValidCachedProfile(userId: String): Boolean {
        return getCachedUserProfile(userId) != null
    }

    override suspend fun getProfileCacheExpiration(): Long {
        return try {
            val preferences = context.dataStore.data.first()
            preferences[PreferencesKeys.PROFILE_CACHE_EXPIRATION] ?: defaultCacheExpiration
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to get profile cache expiration, using default")
            defaultCacheExpiration
        }
    }
}