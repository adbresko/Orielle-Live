package com.orielle.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.manager.SessionManager
import com.orielle.domain.model.JournalEntry
import com.orielle.domain.use_case.GetJournalEntriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isGuest: Boolean = true,
    val isLoading: Boolean = true,
    val journalEntries: List<JournalEntry> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getJournalEntriesUseCase: GetJournalEntriesUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeSessionState()
        fetchJournalEntries()
    }

    private fun observeSessionState() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            sessionManager.isGuest.collect { isGuest ->
                _uiState.value = _uiState.value.copy(isGuest = isGuest, isLoading = false)
            }
        }
    }

    private fun fetchJournalEntries() {
        viewModelScope.launch {
            // The UseCase now returns a simple Flow<List<JournalEntry>>
            getJournalEntriesUseCase()
                .catch { e ->
                    // Catch errors in the flow itself
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "An error occurred",
                        isLoading = false
                    )
                }
                .collect { entries ->
                    // Directly update the state with the list of entries
                    _uiState.value = _uiState.value.copy(
                        journalEntries = entries,
                        isLoading = false
                    )
                }
        }
    }
}