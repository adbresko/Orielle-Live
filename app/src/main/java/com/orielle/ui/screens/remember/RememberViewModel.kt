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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import timber.log.Timber

@HiltViewModel
class RememberViewModel @Inject constructor(
    private val journalRepository: JournalRepository,
    private val chatRepository: ChatRepository,
    private val moodCheckInRepository: MoodCheckInRepository,
    private val sessionManager: SessionManager,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(RememberUiState())
    val uiState: StateFlow<RememberUiState> = _uiState.asStateFlow()

    private var allActivities = listOf<UserActivity>()
    private var currentMonth = Calendar.getInstance()
    private var searchQuery = ""

    // Caching variables
    private var isDataLoaded = false
    private var lastLoadTime = 0L
    private val CACHE_DURATION = 5 * 60 * 1000L // 5 minutes in milliseconds

    init {
        // Initialize with current month to prevent crashes
        currentMonth = Calendar.getInstance()
        val month = currentMonth.get(Calendar.MONTH)
        val year = currentMonth.get(Calendar.YEAR)
        _uiState.value = _uiState.value.copy(
            currentMonthYear = "${getMonthName(month)} $year"
        )
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            try {
                // Load profile data immediately (synchronous)
                loadProfileDataImmediately()

                // Load session state
                observeSessionState()

                // Load remember data
                loadRememberData()

            } catch (e: Exception) {
                Timber.e(e, "❌ RememberViewModel: Error in initialization")
                _uiState.value = _uiState.value.copy(error = "Failed to initialize")
            }
        }
    }

    private suspend fun loadProfileDataImmediately() {
        try {
            val userId = sessionManager.currentUserId.first()
            if (userId != null) {
                val cachedProfile = sessionManager.getCachedUserProfile(userId)
                if (cachedProfile != null) {
                    val userName = cachedProfile.firstName ?: cachedProfile.displayName ?: "User"
                    _uiState.value = _uiState.value.copy(
                        userName = userName,
                        userProfileImageUrl = cachedProfile.profileImageUrl,
                        userLocalImagePath = cachedProfile.localImagePath,
                        userSelectedAvatarId = cachedProfile.selectedAvatarId,
                        userBackgroundColorHex = cachedProfile.backgroundColorHex
                    )
                    android.util.Log.d("RememberViewModel", "✅ Loaded profile immediately - userName: $userName, AvatarId: ${cachedProfile.selectedAvatarId}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading profile data immediately")
        }
    }

    private fun observeSessionState() {
        viewModelScope.launch {
            sessionManager.isGuest.collect { isGuest ->
                android.util.Log.d("RememberViewModel", "🔍 observeSessionState - isGuest: $isGuest")
                _uiState.value = _uiState.value.copy(isLoading = false)
                if (!isGuest) {
                    val userId = sessionManager.currentUserId.first()
                    android.util.Log.d("RememberViewModel", "🔍 Not guest, userId: $userId")
                    if (userId != null) {
                        loadUserProfileData(userId)
                    }
                } else {
                    android.util.Log.d("RememberViewModel", "🔍 Is guest, setting userName to null")
                    _uiState.value = _uiState.value.copy(userName = null)
                }
            }
        }
    }

    fun loadRememberData() {
        viewModelScope.launch {
            // Check if we have cached data that's still fresh
            val currentTime = System.currentTimeMillis()
            if (isDataLoaded && (currentTime - lastLoadTime) < CACHE_DURATION) {
                // Use cached data, just regenerate calendar
                generateCalendarData()
                return@launch
            }

            // Show loading only if we don't have cached data
            if (!isDataLoaded) {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }

            try {
                // Add a small delay to show loading smoothly (only if not cached)
                if (!isDataLoaded) {
                    kotlinx.coroutines.delay(300)
                }
                // Load journal entries and convert to activities
                val journalEntries = try {
                    journalRepository.getJournalEntries().first()
                } catch (e: Exception) {
                    println("DEBUG: Error loading journal entries: ${e.message}")
                    emptyList()
                }
                println("DEBUG: Loaded ${journalEntries.size} journal entries")
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
                        mood = entry.mood,
                        location = entry.location,
                        promptText = entry.promptText
                    )
                }

                // Load conversations and convert to activities
                val conversationsResponse = try {
                    chatRepository.getConversationsForUser().first()
                } catch (e: Exception) {
                    println("DEBUG: Error loading conversations: ${e.message}")
                    Response.Failure(com.orielle.domain.model.AppError.Database)
                }
                val conversationActivities = when (conversationsResponse) {
                    is Response.Success -> {
                        println("DEBUG: Loaded ${conversationsResponse.data.size} conversations")
                        // Show ALL conversations, not just saved ones
                        conversationsResponse.data
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
                    }
                    else -> {
                        println("DEBUG: Failed to load conversations: ${conversationsResponse}")
                        emptyList()
                    }
                }

                // Combine all activities and deduplicate by ID (excluding mood check-ins)
                val allActivitiesList = journalActivities + conversationActivities

                // Debug: Log before deduplication
                println("DEBUG: Before deduplication - Journal: ${journalActivities.size}, Conversations: ${conversationActivities.size}")
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

                // Mark data as loaded and update cache time
                isDataLoaded = true
                lastLoadTime = currentTime

            } catch (e: Exception) {
                println("DEBUG: Error loading data: ${e.message}")
                e.printStackTrace()
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

        // Get today's date once to avoid time-based issues
        val today = Calendar.getInstance()
        val todayYear = today.get(Calendar.YEAR)
        val todayMonth = today.get(Calendar.MONTH)
        val todayDay = today.get(Calendar.DAY_OF_MONTH)

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

            // Check if this is today - compare only date components, not time
            val isToday = year == todayYear && month == todayMonth && day == todayDay

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

            // Debug: Log the boolean flags
            if (dayActivities.isNotEmpty()) {
                println("DEBUG: Day ${month + 1}/${day}/${year} flags: reflect=$hasReflect, ask=$hasAsk")
            }

            calendarDays.add(CalendarDay(
                date = date,
                dayOfMonth = day,
                isToday = isToday,
                activities = dayActivities,
                hasReflectActivity = hasReflect,
                hasAskActivity = hasAsk,
                hasCheckInActivity = false
            ))
        }

        _uiState.value = _uiState.value.copy(
            calendarDays = calendarDays,
            currentMonthYear = "${getMonthName(month)} $year"
        )

        // Debug: Log final calendar state
        println("DEBUG: Generated ${calendarDays.size} calendar days")
        println("DEBUG: Today is ${todayMonth + 1}/${todayDay}/${todayYear}")
        calendarDays.filter { it.activities.isNotEmpty() }.forEach { day ->
            println("DEBUG: Calendar day ${day.dayOfMonth} has ${day.activities.size} activities, flags: reflect=${day.hasReflectActivity}, ask=${day.hasAskActivity}")
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
        // Allow clicking on any day, not just days with activities
        _uiState.value = _uiState.value.copy(
            selectedDay = day,
            showDayDetail = day.activities.isNotEmpty() // Only show detail panel if there are activities
        )
    }

    fun onDayDetailDismiss() {
        _uiState.value = _uiState.value.copy(
            showDayDetail = false,
            selectedDay = null
        )
    }

    fun hideDayDetail() {
        _uiState.value = _uiState.value.copy(
            showDayDetail = false,
            selectedDay = null
        )
    }

    fun showMonthSelector() {
        _uiState.value = _uiState.value.copy(showMonthSelector = true)
    }

    fun hideMonthSelector() {
        _uiState.value = _uiState.value.copy(showMonthSelector = false)
    }

    fun showYearSelector() {
        _uiState.value = _uiState.value.copy(showYearSelector = true)
    }

    fun hideYearSelector() {
        _uiState.value = _uiState.value.copy(showYearSelector = false)
    }

    fun selectMonth(month: Int) {
        currentMonth.set(Calendar.MONTH, month)
        generateCalendarData()
        _uiState.value = _uiState.value.copy(showMonthSelector = false)
    }

    fun selectYear(year: Int) {
        currentMonth.set(Calendar.YEAR, year)
        generateCalendarData()
        _uiState.value = _uiState.value.copy(showYearSelector = false)
    }

    fun clearSearch() {
        searchQuery = ""
        filterActivities()
    }

    private suspend fun loadUserProfileData(userId: String) {
        try {
            android.util.Log.d("RememberViewModel", "🔍 Loading profile data for userId: $userId")
            val cachedProfile = sessionManager.getCachedUserProfile(userId)
            android.util.Log.d("RememberViewModel", "🔍 Cached profile: $cachedProfile")
            if (cachedProfile != null) {
                val userName = cachedProfile.firstName ?: cachedProfile.displayName ?: "User"
                android.util.Log.d("RememberViewModel", "🔍 Setting userName to: $userName")
                _uiState.value = _uiState.value.copy(
                    userName = userName,
                    userProfileImageUrl = cachedProfile.profileImageUrl,
                    userLocalImagePath = cachedProfile.localImagePath,
                    userSelectedAvatarId = cachedProfile.selectedAvatarId,
                    userBackgroundColorHex = cachedProfile.backgroundColorHex
                )
                android.util.Log.d("RememberViewModel", "🔍 Updated uiState - userName: ${_uiState.value.userName}, AvatarId: ${_uiState.value.userSelectedAvatarId}")
            } else {
                android.util.Log.d("RememberViewModel", "🔍 No cached profile found, loading from Firebase")
                refreshUserProfileFromFirebase(userId)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading user profile data")
            _uiState.value = _uiState.value.copy(userName = "User")
        }
    }

    private suspend fun refreshUserProfileFromFirebase(userId: String) {
        try {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val firstName = document.getString("firstName")
                    val displayName = document.getString("displayName")
                    val profileImageUrl = document.getString("profileImageUrl")
                    val localImagePath = document.getString("localImagePath")
                    val selectedAvatarId = document.getString("selectedAvatarId")
                    val backgroundColorHex = document.getString("backgroundColorHex")
                    val userName = firstName ?: displayName ?: "User"
                    _uiState.value = _uiState.value.copy(
                        userName = userName,
                        userProfileImageUrl = profileImageUrl,
                        userLocalImagePath = localImagePath,
                        userSelectedAvatarId = selectedAvatarId,
                        userBackgroundColorHex = backgroundColorHex
                    )

                    // Debug logging
                    android.util.Log.d("RememberViewModel", "Loaded from Firebase - ImageUrl: $profileImageUrl, LocalPath: $localImagePath, AvatarId: $selectedAvatarId, UserName: $userName")
                }
                .addOnFailureListener { exception ->
                    Timber.e(exception, "Failed to fetch user profile from Firebase")
                    _uiState.value = _uiState.value.copy(userName = "User")
                }
        } catch (e: Exception) {
            Timber.e(e, "Exception during Firebase profile fetch")
            _uiState.value = _uiState.value.copy(userName = "User")
        }
    }

    fun refreshUserProfile() {
        viewModelScope.launch {
            try {
                val userId = sessionManager.currentUserId.first()
                if (userId != null) {
                    android.util.Log.d("RememberViewModel", "Refreshing user profile data")
                    loadUserProfileData(userId)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error refreshing user profile")
            }
        }
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
    val showYearSelector: Boolean = false,
    // Profile data
    val userName: String? = null,
    val userProfileImageUrl: String? = null,
    val userLocalImagePath: String? = null,
    val userSelectedAvatarId: String? = null,
    val userBackgroundColorHex: String? = null
)
