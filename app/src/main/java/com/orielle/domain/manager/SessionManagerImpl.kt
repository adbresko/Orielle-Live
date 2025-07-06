package com.orielle.data.manager

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

// At the top level of your file, outside the class definition
internal val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "session_prefs")

