package com.orielle.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.orielle.domain.manager.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey

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
    }

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
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}