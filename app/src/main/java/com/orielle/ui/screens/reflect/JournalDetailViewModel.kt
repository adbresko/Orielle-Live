package com.orielle.ui.screens.reflect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.model.JournalEntry
import com.orielle.domain.repository.JournalRepository
import com.orielle.domain.use_case.GetJournalEntriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JournalDetailViewModel @Inject constructor(
    private val getJournalEntriesUseCase: GetJournalEntriesUseCase,
    private val journalRepository: JournalRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalDetailUiState())
    val uiState: StateFlow<JournalDetailUiState> = _uiState.asStateFlow()

    fun loadEntry(entryId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val entries = getJournalEntriesUseCase().first()
                val entry = entries.find { it.id == entryId }

                _uiState.value = _uiState.value.copy(
                    entry = entry,
                    isLoading = false,
                    error = if (entry == null) "Entry not found" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load entry: ${e.message}"
                )
            }
        }
    }

    fun showDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true)
    }

    fun hideDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false)
    }

    fun deleteEntry(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val entry = _uiState.value.entry ?: return@launch

            try {
                journalRepository.deleteJournalEntry(entry.id)
                _uiState.value = _uiState.value.copy(showDeleteDialog = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete entry: ${e.message}",
                    showDeleteDialog = false
                )
            }
        }
    }
}

data class JournalDetailUiState(
    val entry: JournalEntry? = null,
    val isLoading: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val error: String? = null
)
