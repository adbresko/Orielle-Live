package com.orielle.ui.screens.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.model.MoodCheckIn
import com.orielle.domain.model.Response
import com.orielle.domain.repository.MoodCheckInRepository
import com.orielle.domain.manager.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class InnerWeatherHistoryUiState(
    val isLoading: Boolean = true,
    val moodCheckIns: List<MoodCheckIn> = emptyList(),
    val filteredMoodCheckIns: List<MoodCheckIn> = emptyList(),
    val selectedMoods: Set<String> = emptySet(),
    val selectedTags: Set<String> = emptySet(),
    val selectedDateRange: DateRange? = null,
    val availableMoods: List<String> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val error: String? = null,
    val isSelectionMode: Boolean = false,
    val selectedCheckIns: Set<String> = emptySet()
)

data class DateRange(
    val startDate: Date,
    val endDate: Date
)

@HiltViewModel
class InnerWeatherHistoryViewModel @Inject constructor(
    private val moodCheckInRepository: MoodCheckInRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(InnerWeatherHistoryUiState())
    val uiState: StateFlow<InnerWeatherHistoryUiState> = _uiState.asStateFlow()

    fun loadMoodHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val currentUserId = sessionManager.currentUserId.first() ?: ""
                if (currentUserId.isBlank()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No user session found"
                    )
                    return@launch
                }

                val moodCheckInsResponse = moodCheckInRepository.getMoodCheckInsByUserId(currentUserId).first()
                val moodCheckIns = when (moodCheckInsResponse) {
                    is Response.Success -> moodCheckInsResponse.data.sortedByDescending { it.timestamp }
                    else -> emptyList()
                }

                // Extract available moods and tags for filtering
                val availableMoods = moodCheckIns.map { it.mood }.distinct().sorted()
                val availableTags = moodCheckIns.flatMap { it.tags }.distinct().sorted()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    moodCheckIns = moodCheckIns,
                    filteredMoodCheckIns = moodCheckIns,
                    availableMoods = availableMoods,
                    availableTags = availableTags
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load mood history"
                )
            }
        }
    }

    fun toggleMoodFilter(mood: String) {
        val currentMoods = _uiState.value.selectedMoods.toMutableSet()
        if (currentMoods.contains(mood)) {
            currentMoods.remove(mood)
        } else {
            currentMoods.add(mood)
        }
        _uiState.value = _uiState.value.copy(selectedMoods = currentMoods)
        applyFilters()
    }

    fun toggleTagFilter(tag: String) {
        val currentTags = _uiState.value.selectedTags.toMutableSet()
        if (currentTags.contains(tag)) {
            currentTags.remove(tag)
        } else {
            currentTags.add(tag)
        }
        _uiState.value = _uiState.value.copy(selectedTags = currentTags)
        applyFilters()
    }

    fun setDateRange(dateRange: DateRange?) {
        _uiState.value = _uiState.value.copy(selectedDateRange = dateRange)
        applyFilters()
    }

    fun clearAllFilters() {
        _uiState.value = _uiState.value.copy(
            selectedMoods = emptySet(),
            selectedTags = emptySet(),
            selectedDateRange = null,
            filteredMoodCheckIns = _uiState.value.moodCheckIns
        )
    }

    fun toggleSelectionMode() {
        _uiState.value = _uiState.value.copy(
            isSelectionMode = !_uiState.value.isSelectionMode,
            selectedCheckIns = if (_uiState.value.isSelectionMode) emptySet() else _uiState.value.selectedCheckIns
        )
    }

    fun toggleCheckInSelection(checkInId: String) {
        val currentSelected = _uiState.value.selectedCheckIns.toMutableSet()
        if (currentSelected.contains(checkInId)) {
            currentSelected.remove(checkInId)
        } else {
            currentSelected.add(checkInId)
        }
        _uiState.value = _uiState.value.copy(selectedCheckIns = currentSelected)
    }

    fun deleteSelectedCheckIns() {
        viewModelScope.launch {
            try {
                val selectedIds = _uiState.value.selectedCheckIns
                val checkInsToDelete = _uiState.value.moodCheckIns.filter { it.id in selectedIds }

                // Delete each selected check-in
                checkInsToDelete.forEach { checkIn ->
                    moodCheckInRepository.deleteMoodCheckIn(checkIn)
                }

                // Update state
                _uiState.value = _uiState.value.copy(
                    selectedCheckIns = emptySet(),
                    isSelectionMode = false
                )

                // Reload data
                loadMoodHistory()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete mood check-ins: ${e.message}"
                )
            }
        }
    }

    fun deleteCheckIn(checkInId: String) {
        viewModelScope.launch {
            try {
                val checkInToDelete = _uiState.value.moodCheckIns.find { it.id == checkInId }
                if (checkInToDelete != null) {
                    moodCheckInRepository.deleteMoodCheckIn(checkInToDelete)
                    loadMoodHistory()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to delete mood check-in: ${e.message}"
                )
            }
        }
    }

    private fun applyFilters() {
        val currentState = _uiState.value
        val allCheckIns = currentState.moodCheckIns

        val filtered = allCheckIns.filter { checkIn ->
            // Apply mood filter
            val matchesMood = if (currentState.selectedMoods.isEmpty()) {
                true
            } else {
                currentState.selectedMoods.contains(checkIn.mood)
            }

            // Apply tag filter
            val matchesTags = if (currentState.selectedTags.isEmpty()) {
                true
            } else {
                checkIn.tags.any { tag -> currentState.selectedTags.contains(tag) }
            }

            // Apply date range filter
            val matchesDateRange = if (currentState.selectedDateRange == null) {
                true
            } else {
                val checkInTime = checkIn.timestamp.time
                val startTime = currentState.selectedDateRange.startDate.time
                val endTime = currentState.selectedDateRange.endDate.time
                checkInTime in startTime..endTime
            }

            matchesMood && matchesTags && matchesDateRange
        }

        _uiState.value = currentState.copy(filteredMoodCheckIns = filtered)
    }

    fun formatMoodDate(date: Date): String {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val checkInDate = Calendar.getInstance().apply { time = date }

        return when {
            isSameDay(checkInDate, today) -> "Today"
            isSameDay(checkInDate, yesterday) -> "Yesterday"
            else -> {
                val formatter = SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault())
                formatter.format(date)
            }
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }
}
