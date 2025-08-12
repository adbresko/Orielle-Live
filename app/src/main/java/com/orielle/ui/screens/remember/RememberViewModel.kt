package com.orielle.ui.screens.remember

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.model.ChatConversation
import com.orielle.domain.model.JournalEntry
import com.orielle.domain.model.MoodCheckIn
import com.orielle.domain.model.Response
import com.orielle.domain.repository.ChatRepository
import com.orielle.domain.repository.JournalRepository
import com.orielle.domain.repository.MoodCheckInRepository
import com.orielle.domain.use_case.GetWeeklyMoodViewUseCase
import com.orielle.domain.manager.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class RememberViewModel @Inject constructor(
    private val journalRepository: JournalRepository,
    private val chatRepository: ChatRepository,
    private val moodCheckInRepository: MoodCheckInRepository,
    private val getWeeklyMoodViewUseCase: GetWeeklyMoodViewUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RememberUiState())
    val uiState: StateFlow<RememberUiState> = _uiState.asStateFlow()

    private var originalJournalEntries = listOf<JournalEntry>()
    private var originalConversations = listOf<ChatConversation>()
    private var originalMoodData = listOf<MoodCheckIn>()

    init {
        loadRememberData()
    }

    fun loadRememberData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Load journal entries
                val entries = journalRepository.getJournalEntries().first()
                originalJournalEntries = entries.sortedByDescending { it.timestamp }
                updateUiState()
            } catch (e: Exception) {
                // Handle error silently
            }

            try {
                // Load saved conversations
                val response = chatRepository.getConversationsForUser().first()
                when (response) {
                    is Response.Success -> {
                        originalConversations = response.data
                            .filter { it.isSaved }
                            .sortedByDescending { it.updatedAt }
                        updateUiState()
                    }
                    is Response.Failure -> {
                        // Handle error silently
                    }
                    is Response.Loading -> {
                        // Handle loading silently
                    }
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
                println("Error loading conversations: ${e.message}")
            }

            try {
                // Load weekly mood data
                val weeklyView = getWeeklyMoodViewUseCase().first()
                originalMoodData = weeklyView.days
                    .mapNotNull { it.moodCheckIn }
                    .sortedBy { it.timestamp }
                updateUiState()
            } catch (e: Exception) {
                // Handle error silently
            }

            _uiState.value = _uiState.value.copy(isLoading = false)


        }
    }

    private fun updateUiState() {
        _uiState.value = _uiState.value.copy(
            journalEntries = originalJournalEntries,
            conversations = originalConversations,
            weeklyMoodData = originalMoodData
        )
        // Generate milestone message after updating UI state
        generateMilestoneMessage()
    }

    fun showSearchDialog() {
        _uiState.value = _uiState.value.copy(showSearchDialog = true)
    }

    fun hideSearchDialog() {
        _uiState.value = _uiState.value.copy(showSearchDialog = false)
    }

    fun applyFilters(dateFilter: String?, tagFilter: String?) {
        viewModelScope.launch {
            var filteredJournalEntries = originalJournalEntries
            var filteredConversations = originalConversations

            // Apply date filter
            dateFilter?.let { dateStr ->
                // Parse date filter and apply
                // This is a simplified implementation - you can enhance it
            }

            // Apply tag filter
            tagFilter?.let { tag ->
                filteredJournalEntries = originalJournalEntries.filter { entry ->
                    entry.tags.contains(tag)
                }
                filteredConversations = originalConversations.filter { conversation ->
                    conversation.tags.contains(tag)
                }
            }

            _uiState.value = _uiState.value.copy(
                journalEntries = filteredJournalEntries,
                conversations = filteredConversations
            )
        }
    }

    private fun generateMilestoneMessage() {
        val totalEntries = originalJournalEntries.size
        val totalConversations = originalConversations.count { it.isSaved }

        val message = when {
            totalEntries == 0 && totalConversations == 0 -> null
            totalEntries == 1 && totalConversations == 0 -> "You've written your first reflection. Your journey begins!"
            totalConversations == 1 && totalEntries == 0 -> "You've saved your first conversation. Your story is growing."
            totalEntries >= 7 -> "You've reflected ${totalEntries} times. Consistency builds wisdom."
            totalConversations >= 5 -> "You've saved ${totalConversations} conversations. Your story is growing."
            totalEntries + totalConversations >= 10 -> "You've created ${totalEntries + totalConversations} memories. Keep building your story."
            else -> null
        }

        _uiState.value = _uiState.value.copy(milestoneMessage = message)
    }
}

data class RememberUiState(
    val isLoading: Boolean = false,
    val journalEntries: List<JournalEntry> = emptyList(),
    val conversations: List<ChatConversation> = emptyList(),
    val weeklyMoodData: List<MoodCheckIn> = emptyList(),
    val milestoneMessage: String? = null,
    val showSearchDialog: Boolean = false
)
