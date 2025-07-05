package com.orielle.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.model.JournalEntry
import com.orielle.domain.use_case.GetJournalEntriesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

/**
 * The ViewModel for the HomeScreen.
 * It is responsible for fetching the user's journal entries and providing them
 * as state to the UI.
 *
 * @param getJournalEntriesUseCase The use case for retrieving journal entries.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getJournalEntriesUseCase: GetJournalEntriesUseCase,
) : ViewModel() {

    // --- UI State ---
    private val _journalEntries = MutableStateFlow<List<JournalEntry>>(emptyList())
    val journalEntries = _journalEntries.asStateFlow()

    // You can add other states here later, e.g., for loading or errors
    // private val _isLoading = MutableStateFlow(false)
    // val isLoading = _isLoading.asStateFlow()

    init {
        // When the ViewModel is created, immediately start observing the journal entries.
        fetchJournalEntries()
    }

    private fun fetchJournalEntries() {
        getJournalEntriesUseCase()
            .onEach { entries ->
                _journalEntries.value = entries
            }
            .launchIn(viewModelScope)
    }
}