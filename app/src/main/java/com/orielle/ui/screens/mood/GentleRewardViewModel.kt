package com.orielle.ui.screens.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.data.local.model.QuoteEntity
import com.orielle.data.repository.QuoteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Gentle Reward Screen.
 * Handles quote loading and state management.
 */
@HiltViewModel
class GentleRewardViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GentleRewardUiState())
    val uiState: StateFlow<GentleRewardUiState> = _uiState.asStateFlow()

    // Track recently shown quotes to avoid repetition
    private val recentlyShownQuotes = mutableSetOf<String>()

    init {
        // Initialize quotes on first launch
        initializeQuotes()
    }

    private fun initializeQuotes() {
        viewModelScope.launch {
            try {
                quoteRepository.initializeQuotes()
            } catch (e: Exception) {
                // Handle initialization error silently
                // The error will be shown when trying to load a quote
            }
        }
    }

    fun loadQuoteForMood(mood: String) {
        viewModelScope.launch {
            android.util.Log.d("GentleRewardViewModel", "Loading quote for mood: $mood")
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                // First, check if quotes are loaded
                val totalQuotes = quoteRepository.getTotalQuoteCount()
                val moodQuotes = quoteRepository.getQuoteCountByMood(mood)
                android.util.Log.d("GentleRewardViewModel", "Total quotes in DB: $totalQuotes, Quotes for $mood: $moodQuotes")

                // Get recently shown quotes for this mood to avoid repetition
                val excludeIds = recentlyShownQuotes.toList()

                val quote = quoteRepository.getRandomQuoteByMood(mood, excludeIds)
                android.util.Log.d("GentleRewardViewModel", "Retrieved quote: ${quote?.id ?: "null"}")

                if (quote != null) {
                    // Add to recently shown quotes
                    recentlyShownQuotes.add(quote.id)

                    // If we have too many recent quotes, remove the oldest ones
                    if (recentlyShownQuotes.size > 10) {
                        val quotesToRemove = recentlyShownQuotes.take(5)
                        recentlyShownQuotes.removeAll(quotesToRemove)
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        quote = quote,
                        error = null
                    )
                } else {
                    android.util.Log.w("GentleRewardViewModel", "No quote found for mood: $mood")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        quote = null,
                        error = "No quotes available for $mood mood"
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("GentleRewardViewModel", "Error loading quote for mood: $mood", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    quote = null,
                    error = e.message ?: "Failed to load quote"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

/**
 * UI state for the Gentle Reward Screen.
 */
data class GentleRewardUiState(
    val isLoading: Boolean = false,
    val quote: QuoteEntity? = null,
    val error: String? = null
)
