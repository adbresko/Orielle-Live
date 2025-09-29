package com.orielle.ui.screens.remember

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.model.UserActivity
import com.orielle.domain.model.ActivityType
import com.orielle.domain.model.Response
import com.orielle.domain.repository.JournalRepository
import com.orielle.domain.repository.ChatRepository
import com.orielle.domain.repository.MoodCheckInRepository
import com.orielle.domain.manager.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

data class RememberSearchUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val allActivities: List<UserActivity> = emptyList(),
    val filteredResults: List<UserActivity> = emptyList(),
    val selectedTypes: Set<ActivityType> = emptySet(),
    val selectedMoods: Set<String> = emptySet(),
    val selectedTags: Set<String> = emptySet(),
    val availableMoods: List<String> = emptyList(),
    val availableTags: List<String> = emptyList(),
    val startDate: Date? = null,
    val endDate: Date? = null,
    val error: String? = null
)

@HiltViewModel
class RememberSearchViewModel @Inject constructor(
    private val journalRepository: JournalRepository,
    private val chatRepository: ChatRepository,
    private val moodCheckInRepository: MoodCheckInRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RememberSearchUiState())
    val uiState: StateFlow<RememberSearchUiState> = _uiState.asStateFlow()

    fun loadSearchData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                // Load all activities (journal, chat, mood)
                val allActivities = mutableListOf<UserActivity>()

                // Load journal entries and convert to activities
                val journalEntries = journalRepository.getJournalEntries().first()
                val journalActivities = journalEntries.map { entry ->
                    UserActivity(
                        id = entry.id,
                        userId = entry.userId,
                        activityType = ActivityType.REFLECT,
                        timestamp = entry.timestamp,
                        relatedId = entry.id,
                        title = entry.title,
                        preview = entry.content.take(150) + if (entry.content.length > 150) "..." else "",
                        tags = entry.tags,
                        mood = entry.mood,
                        location = entry.location,
                        promptText = entry.promptText
                    )
                }
                allActivities.addAll(journalActivities)

                // Load chat conversations and convert to activities
                val conversationsResponse = chatRepository.getConversationsForUser().first()
                val conversations = when (conversationsResponse) {
                    is Response.Success -> conversationsResponse.data // Show ALL conversations, not just saved ones
                    else -> emptyList()
                }
                val chatActivities = conversations.map { conversation ->
                    UserActivity(
                        id = conversation.id,
                        userId = conversation.userId,
                        activityType = ActivityType.ASK,
                        timestamp = conversation.createdAt,
                        relatedId = conversation.id,
                        title = conversation.title,
                        preview = conversation.lastMessagePreview?.take(150) + if ((conversation.lastMessagePreview?.length ?: 0) > 150) "..." else "",
                        tags = conversation.tags,
                        mood = null,
                        location = null,
                        promptText = null
                    )
                }
                allActivities.addAll(chatActivities)

                // Load mood check-ins to get mood context for Ask/Reflect entries
                val currentUserId = sessionManager.currentUserId.first() ?: ""
                val moodCheckInsResponse = moodCheckInRepository.getMoodCheckInsByUserId(currentUserId).first()
                val moodCheckIns = when (moodCheckInsResponse) {
                    is Response.Success -> moodCheckInsResponse.data
                    else -> emptyList()
                }

                // Create a map of dates to moods for context lookup
                val moodContextMap = moodCheckIns
                    .groupBy { checkIn ->
                        val calendar = java.util.Calendar.getInstance()
                        calendar.time = checkIn.timestamp
                        Triple(calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))
                    }
                    .mapValues { (_, checkIns) ->
                        // Keep the most recent mood for each day
                        checkIns.maxByOrNull { it.timestamp }?.mood
                    }
                    .filterValues { it != null }
                    .mapValues { (_, mood) -> mood!! }

                // Add mood context to Ask/Reflect activities
                val activitiesWithMoodContext = allActivities.map { activity ->
                    val calendar = java.util.Calendar.getInstance()
                    calendar.time = activity.timestamp
                    val dateKey = Triple(calendar.get(java.util.Calendar.YEAR), calendar.get(java.util.Calendar.MONTH), calendar.get(java.util.Calendar.DAY_OF_MONTH))
                    val moodContext = moodContextMap[dateKey]

                    activity.copy(mood = moodContext)
                }

                // Sort all activities by timestamp (newest first)
                val sortedActivities = activitiesWithMoodContext.sortedByDescending { it.timestamp }

                // Define all available moods (not just from user's data)
                val allAvailableMoods = listOf("Happy", "Sad", "Angry", "Peaceful", "Playful", "Scared", "Shy", "Surprised", "Frustrated")

                // Extract available tags for filters
                val availableTags = allActivities.flatMap { it.tags }.distinct().sorted()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    allActivities = sortedActivities,
                    filteredResults = sortedActivities,
                    availableMoods = allAvailableMoods,
                    availableTags = availableTags
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        applyFilters()
    }

    fun toggleTypeFilter(type: ActivityType) {
        val currentTypes = _uiState.value.selectedTypes.toMutableSet()
        if (currentTypes.contains(type)) {
            currentTypes.remove(type)
        } else {
            currentTypes.add(type)
        }
        _uiState.value = _uiState.value.copy(selectedTypes = currentTypes)
        applyFilters()
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

    private fun applyFilters() {
        val currentState = _uiState.value
        val query = currentState.searchQuery.lowercase().trim()

        val filtered = currentState.allActivities.filter { activity ->
            // Only show Ask and Reflect activities (exclude mood check-ins)
            if (activity.activityType == ActivityType.CHECK_IN) {
                return@filter false
            }

            // Apply text search filter
            val matchesQuery = if (query.isEmpty()) {
                true
            } else {
                activity.title?.lowercase()?.contains(query) == true ||
                        activity.preview?.lowercase()?.contains(query) == true ||
                        activity.tags.any { it.lowercase().contains(query) }
            }

            // Apply type filter (only Ask and Reflect)
            val matchesType = if (currentState.selectedTypes.isEmpty()) {
                true
            } else {
                currentState.selectedTypes.contains(activity.activityType)
            }

            // Apply mood filter - check if the activity was created on a day with the selected mood
            val matchesMood = if (currentState.selectedMoods.isEmpty()) {
                true
            } else {
                activity.mood != null && currentState.selectedMoods.contains(activity.mood)
            }

            // Apply tag filter
            val matchesTags = if (currentState.selectedTags.isEmpty()) {
                true
            } else {
                activity.tags.any { currentState.selectedTags.contains(it) }
            }

            // Apply date range filter
            val matchesDateRange = if (currentState.startDate == null && currentState.endDate == null) {
                true
            } else {
                val activityDate = activity.timestamp
                val afterStart = currentState.startDate?.let { activityDate >= it } ?: true
                val beforeEnd = currentState.endDate?.let { activityDate <= it } ?: true
                afterStart && beforeEnd
            }

            matchesQuery && matchesType && matchesMood && matchesTags && matchesDateRange
        }

        _uiState.value = currentState.copy(filteredResults = filtered)
    }

    fun updateStartDate(date: Date?) {
        _uiState.value = _uiState.value.copy(startDate = date)
        applyFilters()
    }

    fun updateEndDate(date: Date?) {
        _uiState.value = _uiState.value.copy(endDate = date)
        applyFilters()
    }

    fun clearDateRange() {
        _uiState.value = _uiState.value.copy(
            startDate = null,
            endDate = null
        )
        applyFilters()
    }

    fun clearAllFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedTypes = emptySet(),
            selectedMoods = emptySet(),
            selectedTags = emptySet(),
            startDate = null,
            endDate = null,
            filteredResults = _uiState.value.allActivities
        )
    }
}
