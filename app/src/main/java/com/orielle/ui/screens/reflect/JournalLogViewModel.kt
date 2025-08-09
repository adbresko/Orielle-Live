package com.orielle.ui.screens.reflect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.model.JournalEntry
import com.orielle.domain.use_case.GetJournalEntriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class JournalLogViewModel @Inject constructor(
    private val getJournalEntriesUseCase: GetJournalEntriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(JournalLogUiState())
    val uiState: StateFlow<JournalLogUiState> = _uiState.asStateFlow()

    init {
        loadJournalEntries()
    }

    private fun loadJournalEntries() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                getJournalEntriesUseCase().collect { entries ->
                    _uiState.value = _uiState.value.copy(
                        allEntries = entries,
                        filteredEntries = applyCurrentFilter(entries),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load journal entries: ${e.message}"
                )
            }
        }
    }

    fun showFilterDialog() {
        _uiState.value = _uiState.value.copy(showFilterDialog = true)
    }

    fun hideFilterDialog() {
        _uiState.value = _uiState.value.copy(showFilterDialog = false)
    }

    fun applyFilter(dateFilter: String?, tagFilter: String?) {
        _uiState.value = _uiState.value.copy(
            currentDateFilter = dateFilter,
            currentTagFilter = tagFilter,
            filteredEntries = applyCurrentFilter(_uiState.value.allEntries)
        )
    }

    private fun applyCurrentFilter(entries: List<JournalEntry>): List<JournalEntry> {
        var filtered = entries

        // Apply date filter
        _uiState.value.currentDateFilter?.let { dateFilter ->
            val calendar = Calendar.getInstance()
            val now = calendar.time

            when (dateFilter) {
                "today" -> {
                    calendar.time = now
                    val startOfDay = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }.time

                    val endOfDay = calendar.apply {
                        set(Calendar.HOUR_OF_DAY, 23)
                        set(Calendar.MINUTE, 59)
                        set(Calendar.SECOND, 59)
                        set(Calendar.MILLISECOND, 999)
                    }.time

                    filtered = filtered.filter { entry ->
                        entry.timestamp >= startOfDay && entry.timestamp <= endOfDay
                    }
                }
                "week" -> {
                    calendar.time = now
                    calendar.add(Calendar.WEEK_OF_YEAR, -1)
                    val weekAgo = calendar.time

                    filtered = filtered.filter { entry ->
                        entry.timestamp >= weekAgo
                    }
                }
                "month" -> {
                    calendar.time = now
                    calendar.add(Calendar.MONTH, -1)
                    val monthAgo = calendar.time

                    filtered = filtered.filter { entry ->
                        entry.timestamp >= monthAgo
                    }
                }
            }
        }

        // Apply tag filter
        _uiState.value.currentTagFilter?.let { tagFilter ->
            filtered = filtered.filter { entry ->
                entry.tags.any { tag ->
                    tag.contains(tagFilter, ignoreCase = true)
                }
            }
        }

        return filtered
    }

    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            currentDateFilter = null,
            currentTagFilter = null,
            filteredEntries = _uiState.value.allEntries
        )
    }
}

data class JournalLogUiState(
    val allEntries: List<JournalEntry> = emptyList(),
    val filteredEntries: List<JournalEntry> = emptyList(),
    val isLoading: Boolean = false,
    val showFilterDialog: Boolean = false,
    val currentDateFilter: String? = null,
    val currentTagFilter: String? = null,
    val error: String? = null
)
