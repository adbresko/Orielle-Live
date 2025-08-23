package com.orielle.ui.theme

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

@Singleton
class ThemeManager @Inject constructor(
    private val context: Context
) {

    companion object {
        private val IS_DARK_THEME_KEY = booleanPreferencesKey("is_dark_theme")
    }

    /**
     * Get the current theme preference
     * @return true for dark theme, false for light theme
     */
    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_THEME_KEY] ?: false // Default to light theme
        }

    /**
     * Set the theme preference
     * @param isDark true for dark theme, false for light theme
     */
    suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_THEME_KEY] = isDark
        }
    }

    /**
     * Toggle between light and dark themes
     */
    suspend fun toggleTheme() {
        context.dataStore.edit { preferences ->
            val currentTheme = preferences[IS_DARK_THEME_KEY] ?: false
            preferences[IS_DARK_THEME_KEY] = !currentTheme
        }
    }
}
