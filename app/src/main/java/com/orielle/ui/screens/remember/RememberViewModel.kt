package com.orielle.ui.screens.remember

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.model.*
import com.orielle.domain.repository.ChatRepository
import com.orielle.domain.repository.JournalRepository
import com.orielle.domain.repository.MoodCheckInRepository
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
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RememberUiState())
    val uiState: StateFlow<RememberUiState> = _uiState.asStateFlow()

    private var allActivities = listOf<UserActivity>()
    private var currentMonth = Calendar.getInstance()
    private var searchQuery = ""

    init {
        // Initialize with current month to prevent crashes
        val calendar = Calendar.getInstance()
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        _uiState.value = _uiState.value.copy(
            currentMonthYear = "${getMonthName(month)} $year"
        )
        loadRememberData()
    }

    fun loadRememberData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
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
                        preview = entry.content.take(100),
                        tags = entry.tags,
                        mood = entry.mood
                    )
                }

                // Load conversations and convert to activities
                val conversationsResponse = chatRepository.getConversationsForUser().first()
                val conversationActivities = when (conversationsResponse) {
                    is Response.Success -> conversationsResponse.data
                        .filter { it.isSaved }
                        .map { conversation ->
                            UserActivity(
                                id = conversation.id,
                                userId = conversation.userId,
                                activityType = ActivityType.ASK,
                                timestamp = conversation.updatedAt,
                                relatedId = conversation.id,
                                title = conversation.title,
                                preview = conversation.lastMessagePreview,
                                tags = conversation.tags
                            )
                        }
                    else -> emptyList()
                }

                // Load mood check-ins and convert to activities
                val currentUserId = sessionManager.currentUserId.first() ?: ""
                val moodCheckInsResponse = moodCheckInRepository.getMoodCheckInsByUserId(currentUserId).first()
                val moodCheckIns = when (moodCheckInsResponse) {
                    is Response.Success -> moodCheckInsResponse.data
                    else -> emptyList()
                }

                // Debug: Log mood check-ins specifically
                println("DEBUG: Mood check-ins loaded: ${moodCheckIns.size}")
                moodCheckIns.forEach { checkIn ->
                    val cal = Calendar.getInstance()
                    cal.time = checkIn.timestamp
                    println("DEBUG: Mood check-in on ${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.YEAR)}: ${checkIn.id} - ${checkIn.mood}")
                }

                val moodActivities = moodCheckIns.map { checkIn ->
                    UserActivity(
                        id = checkIn.id,
                        userId = checkIn.userId,
                        activityType = ActivityType.CHECK_IN,
                        timestamp = checkIn.timestamp,
                        relatedId = checkIn.id,
                        title = "Mood Check-in",
                        preview = "Mood: ${checkIn.mood}",
                        tags = checkIn.tags,
                        mood = checkIn.mood
                    )
                }

                // Combine all activities and deduplicate by ID
                val allActivitiesList = journalActivities + conversationActivities + moodActivities

                // Debug: Log before deduplication
                println("DEBUG: Before deduplication - Journal: ${journalActivities.size}, Conversations: ${conversationActivities.size}, Mood: ${moodActivities.size}")
                println("DEBUG: Total before deduplication: ${allActivitiesList.size}")

                // More aggressive deduplication - check for same day, same type, same user
                val deduplicatedActivities = allActivitiesList.groupBy { activity ->
                    val cal = Calendar.getInstance()
                    cal.time = activity.timestamp
                    Triple(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                }.mapValues { (_, activities) ->
                    // For each day, keep only one activity of each type
                    activities.groupBy { it.activityType }.mapValues { (_, typeActivities) ->
                        typeActivities.first() // Keep the first one of each type
                    }.values.toList()
                }.values.flatten()

                allActivities = deduplicatedActivities

                // Debug: Log after deduplication
                println("DEBUG: After deduplication: ${allActivities.size}")
                allActivities.forEach { activity ->
                    val cal = Calendar.getInstance()
                    cal.time = activity.timestamp
                    println("DEBUG: Final activity on ${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.YEAR)}: ${activity.activityType} - ${activity.id}")
                }

                // Generate calendar data
                generateCalendarData()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Failed to load data: ${e.message}"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }



    private fun generateCalendarData() {
        val calendar = Calendar.getInstance()
        val year = currentMonth.get(Calendar.YEAR)
        val month = currentMonth.get(Calendar.MONTH)

        // Set to first day of month
        calendar.set(year, month, 1)
        val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        // Get last day of month
        calendar.set(year, month, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val calendarDays = mutableListOf<CalendarDay>()

        // Add empty days for padding at start (Sunday = 1, so we need to adjust)
        val startPadding = if (firstDayOfWeek == Calendar.SUNDAY) 0 else firstDayOfWeek - 1
        for (i in 0 until startPadding) {
            calendarDays.add(CalendarDay(
                date = Date(),
                dayOfMonth = 0,
                isToday = false
            ))
        }

        // Add days of the month
        for (day in 1..lastDayOfMonth) {
            calendar.set(year, month, day)
            val date = calendar.time

            // Check if this is today
            val today = Calendar.getInstance()
            val isToday = calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                    calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                    calendar.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)

            // Get activities for this day
            val dayActivities = allActivities.filter { activity ->
                val activityCal = Calendar.getInstance()
                activityCal.time = activity.timestamp
                val sameYear = activityCal.get(Calendar.YEAR) == year
                val sameMonth = activityCal.get(Calendar.MONTH) == month
                val sameDay = activityCal.get(Calendar.DAY_OF_MONTH) == day

                // Debug: Log each activity check
                if (sameYear && sameMonth) {
                    println("DEBUG: Checking activity ${activity.id} for day $day: year=$sameYear, month=$sameMonth, day=$sameDay")
                }

                sameYear && sameMonth && sameDay
            }

            // Debug: Log activities for specific days
            if (dayActivities.isNotEmpty()) {
                println("DEBUG: Day ${month + 1}/${day}/${year} has ${dayActivities.size} activities:")
                dayActivities.forEach { activity ->
                    println("  - ${activity.activityType}: ${activity.id}")
                }
            }

            val hasReflect = dayActivities.any { it.activityType == ActivityType.REFLECT }
            val hasAsk = dayActivities.any { it.activityType == ActivityType.ASK }
            val hasCheckIn = dayActivities.any { it.activityType == ActivityType.CHECK_IN }

            // Debug: Log the boolean flags
            if (dayActivities.isNotEmpty()) {
                println("DEBUG: Day ${month + 1}/${day}/${year} flags: reflect=$hasReflect, ask=$hasAsk, checkIn=$hasCheckIn")
            }

            calendarDays.add(CalendarDay(
                date = date,
                dayOfMonth = day,
                isToday = isToday,
                activities = dayActivities,
                hasReflectActivity = hasReflect,
                hasAskActivity = hasAsk,
                hasCheckInActivity = hasCheckIn
            ))
        }

        _uiState.value = _uiState.value.copy(
            calendarDays = calendarDays,
            currentMonthYear = "${getMonthName(month)} $year"
        )

        // Debug: Log final calendar state
        println("DEBUG: Generated ${calendarDays.size} calendar days")
        calendarDays.filter { it.activities.isNotEmpty() }.forEach { day ->
            println("DEBUG: Calendar day ${day.dayOfMonth} has ${day.activities.size} activities, flags: reflect=${day.hasReflectActivity}, ask=${day.hasAskActivity}, checkIn=${day.hasCheckInActivity}")
        }
    }

    private fun getMonthName(month: Int): String {
        val monthNames = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return monthNames[month]
    }

    fun nextMonth() {
        currentMonth.add(Calendar.MONTH, 1)
        generateCalendarData()
    }

    fun previousMonth() {
        currentMonth.add(Calendar.MONTH, -1)
        generateCalendarData()
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        filterActivities()
    }

    private fun filterActivities() {
        if (searchQuery.isBlank()) {
            generateCalendarData()
            return
        }

        val filteredActivities = allActivities.filter { activity ->
            activity.title?.contains(searchQuery, ignoreCase = true) == true ||
                    activity.preview?.contains(searchQuery, ignoreCase = true) == true ||
                    activity.tags.any { it.contains(searchQuery, ignoreCase = true) } ||
                    activity.mood?.contains(searchQuery, ignoreCase = true) == true
        }

        // Regenerate calendar with filtered activities
        val originalActivities = allActivities
        allActivities = filteredActivities
        generateCalendarData()
        allActivities = originalActivities
    }

    fun onDayClick(day: CalendarDay) {
        if (day.activities.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                selectedDay = day,
                showDayDetail = true
            )
        }
    }

    fun hideDayDetail() {
        _uiState.value = _uiState.value.copy(
            showDayDetail = false,
            selectedDay = null
        )
    }

    fun showMonthSelector() {
        // For now, just cycle through months
        currentMonth.add(Calendar.MONTH, 1)
        generateCalendarData()
        _uiState.value = _uiState.value.copy(showMonthSelector = false)
    }

    fun hideMonthSelector() {
        _uiState.value = _uiState.value.copy(showMonthSelector = false)
    }

    fun showYearSelector() {
        // For now, just cycle through years
        currentMonth.add(Calendar.YEAR, 1)
        generateCalendarData()
        _uiState.value = _uiState.value.copy(showYearSelector = false)
    }

    fun hideYearSelector() {
        _uiState.value = _uiState.value.copy(showYearSelector = false)
    }

    fun clearSearch() {
        searchQuery = ""
        filterActivities()
    }
}

data class RememberUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val calendarDays: List<CalendarDay> = emptyList(),
    val currentMonthYear: String = "",
    val selectedDay: CalendarDay? = null,
    val showDayDetail: Boolean = false,
    val showMonthSelector: Boolean = false,
    val showYearSelector: Boolean = false
)
