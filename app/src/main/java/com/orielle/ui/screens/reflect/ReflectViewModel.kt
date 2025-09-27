package com.orielle.ui.screens.reflect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.model.DefaultJournalPrompts
import com.orielle.domain.model.JournalEntry
import com.orielle.domain.use_case.GetJournalEntriesUseCase
import com.orielle.domain.manager.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReflectViewModel @Inject constructor(
    private val getJournalEntriesUseCase: GetJournalEntriesUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReflectUiState())
    val uiState: StateFlow<ReflectUiState> = _uiState.asStateFlow()

    init {
        loadTodaysPrompt()
        loadLookBackEntry()
        loadCachedUserProfile()
    }

    private fun loadTodaysPrompt() {
        val todaysPrompt = DefaultJournalPrompts.getTodaysPrompt()
        _uiState.value = _uiState.value.copy(todaysPrompt = todaysPrompt.text)
    }

    private fun loadLookBackEntry() {
        viewModelScope.launch {
            getJournalEntriesUseCase().collect { entries ->
                val lookBackEntry = findLookBackEntry(entries)
                _uiState.value = _uiState.value.copy(lookBackEntry = lookBackEntry)
            }
        }
    }

    private fun findLookBackEntry(entries: List<JournalEntry>): JournalEntry? {
        if (entries.isEmpty()) return null

        val calendar = Calendar.getInstance()
        val today = calendar.time

        // Try to find an entry from 1 week ago, 1 month ago, or 1 year ago
        val lookBackDates = listOf(7, 30, 365) // days ago

        for (daysAgo in lookBackDates) {
            calendar.time = today
            calendar.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val targetDate = calendar.time

            // Find entries from that date (within the same day)
            val targetEntries = entries.filter { entry ->
                val entryCalendar = Calendar.getInstance().apply { time = entry.timestamp }
                val targetCalendar = Calendar.getInstance().apply { time = targetDate }

                entryCalendar.get(Calendar.YEAR) == targetCalendar.get(Calendar.YEAR) &&
                        entryCalendar.get(Calendar.DAY_OF_YEAR) == targetCalendar.get(Calendar.DAY_OF_YEAR)
            }

            if (targetEntries.isNotEmpty()) {
                return targetEntries.first() // Return the first entry from that date
            }
        }

        return null
    }

    private fun loadCachedUserProfile() {
        viewModelScope.launch {
            try {
                val userId = sessionManager.currentUserId.first()
                if (userId != null && !sessionManager.isGuest.first()) {
                    val cachedProfile = sessionManager.getCachedUserProfile(userId)
                    if (cachedProfile != null) {
                        _uiState.value = _uiState.value.copy(
                            userName = cachedProfile.displayName ?: cachedProfile.firstName,
                            userProfileImageUrl = cachedProfile.profileImageUrl,
                            userLocalImagePath = cachedProfile.localImagePath,
                            userSelectedAvatarId = cachedProfile.selectedAvatarId
                        )
                        Timber.d("📋 ReflectViewModel: Loaded cached profile data for user: $userId")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ ReflectViewModel: Failed to load cached user profile")
            }
        }
    }

    fun refreshUserProfile() {
        viewModelScope.launch {
            try {
                val userId = sessionManager.currentUserId.first()
                if (userId != null) {
                    android.util.Log.d("ReflectViewModel", "Refreshing user profile data")
                    loadCachedUserProfile()
                }
            } catch (e: Exception) {
                Timber.e(e, "Error refreshing user profile")
            }
        }
    }
}

data class ReflectUiState(
    val todaysPrompt: String = "",
    val lookBackEntry: JournalEntry? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    // Profile data for avatar display
    val userName: String? = null,
    val userProfileImageUrl: String? = null,
    val userLocalImagePath: String? = null,
    val userSelectedAvatarId: String? = null
)
