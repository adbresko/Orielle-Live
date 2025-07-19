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

// DataStore setup - make it internal so it can be accessed within the same module
internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "orielle_preferences")

@Singleton
class SessionManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
) : SessionManager {

    private object PreferencesKeys {
        val GUEST_UUID_KEY = stringPreferencesKey("guest_uuid")
        val LAST_CHECKIN_TIMESTAMP_KEY = stringPreferencesKey("last_checkin_timestamp")
    }

    override val currentUserId: Flow<String?> = context.dataStore.data
        .map { preferences ->
            // Prioritize Firebase UID. If it's null, check for a guest UUID.
            auth.currentUser?.uid ?: preferences[PreferencesKeys.GUEST_UUID_KEY]
        }

    override val isGuest: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            // A user is a guest if there's no Firebase user AND a guest UUID exists.
            auth.currentUser == null && preferences[PreferencesKeys.GUEST_UUID_KEY] != null
        }

    override suspend fun startGuestSession() {
        context.dataStore.edit { preferences ->
            // Only create a new UUID if one doesn't already exist.
            if (preferences[PreferencesKeys.GUEST_UUID_KEY] == null) {
                preferences[PreferencesKeys.GUEST_UUID_KEY] = UUID.randomUUID().toString()
            }
        }
    }

    override suspend fun endGuestSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.GUEST_UUID_KEY)
        }
    }

    override suspend fun setLastCheckInTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_CHECKIN_TIMESTAMP_KEY] = timestamp.toString()
        }
    }

    override suspend fun getLastCheckInTimestamp(): Long? {
        val preferences = context.dataStore.data.first()
        val timestampString = preferences[PreferencesKeys.LAST_CHECKIN_TIMESTAMP_KEY]
        return timestampString?.toLongOrNull()
    }

    override suspend fun clearLastCheckInTimestamp() {
        context.dataStore.edit { preferences ->
            preferences.remove(PreferencesKeys.LAST_CHECKIN_TIMESTAMP_KEY)
        }
    }
}