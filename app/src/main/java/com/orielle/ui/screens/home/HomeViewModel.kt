package com.orielle.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.manager.SessionManager
import com.orielle.domain.model.JournalEntry
import com.orielle.domain.model.WeeklyMoodView
import com.orielle.domain.use_case.GetJournalEntriesUseCase
import com.orielle.domain.use_case.HasMoodCheckInForDateUseCase
import com.orielle.domain.use_case.GetWeeklyMoodViewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.orielle.domain.model.AppError
import com.orielle.domain.model.Response
import timber.log.Timber
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import com.orielle.ui.util.UiEvent
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit
import androidx.navigation.NavController

data class HomeUiState(
    val isGuest: Boolean = true,
    val isLoading: Boolean = true,
    val isInitializing: Boolean = true, // Add specific initialization state
    val journalEntries: List<JournalEntry> = emptyList(),
    val error: String? = null,
    val userName: String? = null, // Added userName property
    val isPremium: Boolean = false, // Added isPremium property
    val needsMoodCheckIn: Boolean = false, // Added mood check-in status
    val weeklyMoodView: WeeklyMoodView = WeeklyMoodView(emptyList(), 0), // Added weekly mood view
    // Profile data for efficient caching
    val userProfileImageUrl: String? = null,
    val userLocalImagePath: String? = null,
    val userSelectedAvatarId: String? = null
)

// Dashboard state for UI
sealed class DashboardState {
    object Loading : DashboardState() // Loading state to prevent flicker
    object Initial : DashboardState() // Minimalist, needs check-in
    object Unfolded : DashboardState() // Full dashboard
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getJournalEntriesUseCase: GetJournalEntriesUseCase,
    private val hasMoodCheckInForDateUseCase: HasMoodCheckInForDateUseCase,
    private val getWeeklyMoodViewUseCase: GetWeeklyMoodViewUseCase,
    private val sessionManager: SessionManager,
    private val firestore: FirebaseFirestore, // Inject Firestore
    private val auth: FirebaseAuth // Inject FirebaseAuth for log out
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    // Add a log out event
    private val _logOutEvent = MutableSharedFlow<Unit>()
    val logOutEvent = _logOutEvent.asSharedFlow()

    private val _dashboardState = MutableStateFlow<DashboardState>(DashboardState.Loading)
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable, "Unhandled coroutine exception in HomeViewModel")
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar("Unexpected error: ${throwable.localizedMessage ?: "Unknown error"}"))
        }
    }

    init {
        // Start with loading state
        _uiState.value = _uiState.value.copy(isInitializing = true)

        // Perform initial data loading in parallel
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                Timber.d("🚀 HomeViewModel: Starting parallel data initialization")

                // Start all data loading operations in parallel
                launch { observeSessionState() }
                launch { fetchJournalEntries() }
                val moodCheckJob = launch { checkMoodCheckInStatus() }
                val dashboardJob = launch { checkDashboardState() }
                launch { fetchWeeklyMoodView() }

                // Wait for critical data (mood check status and dashboard state) to complete
                moodCheckJob.join()
                dashboardJob.join()

                // Mark initialization as complete
                _uiState.value = _uiState.value.copy(isInitializing = false)
                Timber.d("✅ HomeViewModel: Initial data loading complete")

                // Small delay to ensure smooth transition and prevent flicker
                kotlinx.coroutines.delay(100)

                // Ensure dashboard state is properly set after all data is loaded
                if (_dashboardState.value == DashboardState.Loading) {
                    // If dashboard state is still loading, check it one more time
                    checkDashboardState()
                }

            } catch (e: Exception) {
                Timber.e(e, "❌ HomeViewModel: Error during initialization")
                _uiState.value = _uiState.value.copy(isInitializing = false)
            }
        }
    }

    private fun observeSessionState() {
        viewModelScope.launch(coroutineExceptionHandler) {
            _uiState.value = _uiState.value.copy(isLoading = true)

            sessionManager.isGuest.collect { isGuest ->
                _uiState.value = _uiState.value.copy(isGuest = isGuest, isLoading = false)
                if (!isGuest) {
                    // Try to get cached profile data first, then fetch from Firebase if needed
                    val userId = sessionManager.currentUserId.first()
                    if (userId != null) {
                        loadUserProfileData(userId)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(userName = null, isPremium = false)
                }
            }
        }
    }

    private suspend fun loadUserProfileData(userId: String) {
        try {
            // First, try to get cached profile data
            val cachedProfile = sessionManager.getCachedUserProfile(userId)

            if (cachedProfile != null) {
                // Use cached data immediately
                val userName = cachedProfile.firstName ?: cachedProfile.displayName ?: "User"
                _uiState.value = _uiState.value.copy(
                    userName = userName,
                    isPremium = cachedProfile.isPremium
                )
                Timber.d("✅ HomeViewModel: Used cached profile data for: $userId")

                // Check if cache is getting stale (older than 30 minutes) and refresh in background
                val cacheAge = System.currentTimeMillis() - cachedProfile.cachedAt
                if (cacheAge > 1800000) { // 30 minutes
                    Timber.d("🔄 HomeViewModel: Cache is getting stale, refreshing in background")
                    refreshUserProfileFromFirebase(userId)
                }
            } else {
                // No cached data, fetch from Firebase
                Timber.d("🌐 HomeViewModel: No cached profile, fetching from Firebase")
                refreshUserProfileFromFirebase(userId)
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ HomeViewModel: Error loading user profile data")
            _uiState.value = _uiState.value.copy(userName = "User", isPremium = false)
        }
    }

    private suspend fun refreshUserProfileFromFirebase(userId: String) {
        try {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val firstName = document.getString("firstName")
                    val displayName = document.getString("displayName")
                    val isPremium = document.getBoolean("premium") == true
                    val email = document.getString("email")
                    val profileImageUrl = document.getString("profileImageUrl")
                    val notificationsEnabled = document.getBoolean("notificationsEnabled") ?: true
                    val twoFactorEnabled = document.getBoolean("twoFactorEnabled") ?: false

                    // Use firstName for personalized greetings, fallback to displayName, then "User"
                    val userName = firstName ?: displayName ?: "User"

                    // Update UI state
                    _uiState.value = _uiState.value.copy(userName = userName, isPremium = isPremium)

                    // Cache the profile data for future use
                    viewModelScope.launch {
                        sessionManager.cacheUserProfile(
                            userId = userId,
                            firstName = firstName,
                            displayName = displayName,
                            email = email,
                            profileImageUrl = profileImageUrl,
                            isPremium = isPremium,
                            notificationsEnabled = notificationsEnabled,
                            twoFactorEnabled = twoFactorEnabled
                        )
                    }

                    Timber.d("✅ HomeViewModel: Fetched and cached profile from Firebase for: $userId")
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "❌ HomeViewModel: Failed to fetch user profile from Firebase")
                    _uiState.value = _uiState.value.copy(userName = "User", isPremium = false)
                }
        } catch (e: Exception) {
            Timber.e(e, "❌ HomeViewModel: Exception during Firebase profile fetch")
            _uiState.value = _uiState.value.copy(userName = "User", isPremium = false)
        }
    }

    private fun fetchJournalEntries() {
        viewModelScope.launch(coroutineExceptionHandler) {
            getJournalEntriesUseCase()
                .catch { e ->
                    Timber.e(e, "Error fetching journal entries")
                    _uiState.value = _uiState.value.copy(
                        error = e.message ?: "An error occurred",
                        isLoading = false
                    )
                    _eventFlow.emit(UiEvent.ShowSnackbar(AppError.Database.toUserMessage()))
                }
                .collect { entries ->
                    _uiState.value = _uiState.value.copy(
                        journalEntries = entries,
                        isLoading = false
                    )
                }
        }
    }

    fun retryFetch() {
        fetchJournalEntries()
    }

    private fun checkMoodCheckInStatus() {
        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                val userId = sessionManager.currentUserId.first()
                if (userId != null && !sessionManager.isGuest.first()) {
                    // OPTIMIZATION: Check cache first for instant response
                    val cachedStatus = sessionManager.hasCachedMoodCheckInToday(userId)

                    if (cachedStatus != null) {
                        // Instant response from cache - no loading animation needed
                        _uiState.value = _uiState.value.copy(
                            needsMoodCheckIn = !cachedStatus
                        )
                        Timber.d("⚡ HomeViewModel: Instant mood check-in status from cache - needsCheckIn: ${!cachedStatus}")
                        return@launch
                    }

                    // Fallback to database if no cache (but this should rarely happen)
                    val today = java.util.Date()
                    val result = hasMoodCheckInForDateUseCase(userId, today)

                    when (result) {
                        is com.orielle.domain.model.Response.Success -> {
                            val hasCheckedIn = result.data
                            _uiState.value = _uiState.value.copy(needsMoodCheckIn = !hasCheckedIn)

                            // Cache the result for future instant access
                            if (hasCheckedIn) {
                                val dateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(today)
                                sessionManager.cacheMoodCheckInCompleted(userId, dateString)
                            }

                            Timber.d("📋 HomeViewModel: Mood check-in status from database - needsCheckIn: ${!hasCheckedIn}")
                        }
                        is com.orielle.domain.model.Response.Failure -> {
                            Timber.e(result.exception, "Error checking mood check-in status")
                            _uiState.value = _uiState.value.copy(needsMoodCheckIn = false)
                        }
                        is com.orielle.domain.model.Response.Loading -> { /* Optionally handle loading */ }
                    }
                } else {
                    _uiState.value = _uiState.value.copy(needsMoodCheckIn = false)
                }
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error checking mood check-in status")
                _uiState.value = _uiState.value.copy(needsMoodCheckIn = false)
            }
        }
    }

    private fun checkDashboardState() {
        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                val userId = sessionManager.currentUserId.first()
                if (userId != null && !sessionManager.isGuest.first()) {
                    // OPTIMIZATION: Check cache first for instant dashboard state
                    val cachedStatus = sessionManager.hasCachedMoodCheckInToday(userId)

                    if (cachedStatus != null) {
                        // Instant dashboard state from cache - no loading needed
                        val newState = if (cachedStatus) DashboardState.Unfolded else DashboardState.Initial
                        _dashboardState.value = newState
                        Timber.d("⚡ HomeViewModel: Instant dashboard state from cache - ${if (cachedStatus) "Unfolded" else "Initial"}")
                        return@launch
                    }

                    // Fallback to database if no cache
                    val today = java.util.Date()
                    val result = hasMoodCheckInForDateUseCase(userId, today)

                    when (result) {
                        is com.orielle.domain.model.Response.Success -> {
                            val hasCheckedIn = result.data
                            val newState = if (hasCheckedIn) DashboardState.Unfolded else DashboardState.Initial

                            // Small delay for smooth transition if coming from loading state
                            if (_dashboardState.value == DashboardState.Loading) {
                                kotlinx.coroutines.delay(150)
                            }

                            _dashboardState.value = newState

                            // Cache the result for future instant access
                            if (hasCheckedIn) {
                                val dateString = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(today)
                                sessionManager.cacheMoodCheckInCompleted(userId, dateString)
                            }

                            Timber.d("📋 HomeViewModel: Dashboard state from database - ${if (hasCheckedIn) "Unfolded" else "Initial"}")
                        }
                        is com.orielle.domain.model.Response.Failure -> {
                            Timber.e(result.exception, "Error checking mood check-in for dashboard state")
                            _dashboardState.value = DashboardState.Initial
                        }
                        is com.orielle.domain.model.Response.Loading -> {
                            // Keep current state while loading
                        }
                    }
                } else {
                    // Guest users always see initial state (can still do check-ins)
                    _dashboardState.value = DashboardState.Initial
                }
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error checking dashboard state")
                _dashboardState.value = DashboardState.Initial
            }
        }
    }

    fun onCheckInCompletedOrSkipped() {
        viewModelScope.launch(coroutineExceptionHandler) {
            val userId = sessionManager.currentUserId.first()
            if (userId != null) {
                // CRITICAL: Cache the mood check-in completion immediately
                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                sessionManager.cacheMoodCheckInCompleted(userId, today)

                // Update UI state instantly
                _uiState.value = _uiState.value.copy(needsMoodCheckIn = false)
                _dashboardState.value = DashboardState.Unfolded

                Timber.d("✅ HomeViewModel: Mood check-in completed and cached for user: $userId")
            }

            sessionManager.setLastCheckInTimestamp(System.currentTimeMillis())
            // Refresh mood data after check-in (dashboard state already updated above)
            fetchWeeklyMoodView()
        }
    }

    fun refreshHomeData() {
        Timber.d("🔄 HomeViewModel: Refreshing home data after screen return")
        viewModelScope.launch(coroutineExceptionHandler) {
            // OPTIMIZATION: Load cached data first for instant UI, then refresh in background
            val userId = sessionManager.currentUserId.first()

            // 1. Instant profile data from cache (no network call needed)
            if (userId != null && !sessionManager.isGuest.first()) {
                loadCachedUserProfile()
            }

            // 2. Instant mood check-in status from cache (no database query needed)
            checkMoodCheckInStatus()

            // 3. Instant dashboard state from cache (no database query needed)
            checkDashboardState()

            // 4. Background refresh for mood view (non-critical for initial display)
            launch { fetchWeeklyMoodView() }

            Timber.d("⚡ HomeViewModel: Critical data loaded instantly from cache")
        }
    }

    fun logOut(navController: NavController?) {
        viewModelScope.launch {
            try {
                // Clear all cached data before signing out
                val userId = sessionManager.currentUserId.first()
                if (userId != null) {
                    sessionManager.clearCachedUserProfile(userId)
                    sessionManager.clearCachedMoodCheckIn(userId)
                    Timber.d("🗑️ Cleared all cached data on logout for: $userId")
                }

                auth.signOut()
                sessionManager.endGuestSession()
                navController?.navigate("sign_in") {
                    popUpTo("home_graph") { inclusive = true }
                }
            } catch (e: Exception) {
                Timber.e(e, "❌ Error during logout")
                // Continue with logout even if cache clearing fails
                auth.signOut()
                sessionManager.endGuestSession()
                navController?.navigate("sign_in") {
                    popUpTo("home_graph") { inclusive = true }
                }
            }
        }
    }

    fun refreshUserProfile() {
        observeSessionState()
    }

    /**
     * Loads cached user profile data from SessionManager for efficient display
     * This prevents unnecessary network calls and provides instant UI updates
     */
    private suspend fun loadCachedUserProfile() {
        try {
            val userId = sessionManager.currentUserId.first()
            if (userId != null && !sessionManager.isGuest.first()) {
                val cachedProfile = sessionManager.getCachedUserProfile(userId)
                if (cachedProfile != null) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            userName = cachedProfile.displayName ?: cachedProfile.firstName,
                            userProfileImageUrl = cachedProfile.profileImageUrl,
                            userLocalImagePath = cachedProfile.localImagePath,
                            userSelectedAvatarId = cachedProfile.selectedAvatarId,
                            isPremium = cachedProfile.isPremium
                            // Note: Don't override needsMoodCheckIn - let checkMoodCheckInStatus() handle that
                        )
                    }
                    Timber.d("📋 Loaded cached profile data for user: $userId")
                } else {
                    // If no cached data, fetch from Firebase and cache it
                    refreshUserProfileFromFirebase(userId)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to load cached user profile")
        }
    }

    private fun fetchWeeklyMoodView() {
        viewModelScope.launch(coroutineExceptionHandler) {
            println("HomeViewModel: Starting to fetch weekly mood view")
            getWeeklyMoodViewUseCase().collect { weeklyView ->
                println("HomeViewModel: Received weekly view with ${weeklyView.days.size} days")
                weeklyView.days.forEachIndexed { index, day ->
                    println("HomeViewModel: Day $index: ${day.dayLabel}, isToday: ${day.isToday}")
                }
                _uiState.value = _uiState.value.copy(weeklyMoodView = weeklyView)
            }
        }
    }
}

fun AppError.toUserMessage(): String = when (this) {
    AppError.Network -> "No internet connection."
    AppError.Auth -> "Authentication failed."
    AppError.Database -> "A database error occurred."
    AppError.NotFound -> "Requested resource not found."
    AppError.Permission -> "You do not have permission to perform this action."
    is AppError.Custom -> this.message
    AppError.Unknown -> "An unknown error occurred."
}