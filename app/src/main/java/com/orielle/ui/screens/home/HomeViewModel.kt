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
    val journalEntries: List<JournalEntry> = emptyList(),
    val error: String? = null,
    val userName: String? = null,
    val isPremium: Boolean = false,
    val needsMoodCheckIn: Boolean = false,
    val weeklyMoodView: WeeklyMoodView = WeeklyMoodView(emptyList(), 0),
    val userProfileImageUrl: String? = null,
    val userLocalImagePath: String? = null,
    val userSelectedAvatarId: String? = null,
    val userBackgroundColorHex: String? = null
)

// Dashboard state for UI
sealed class DashboardState {
    object Loading : DashboardState()
    object Initial : DashboardState()
    object Unfolded : DashboardState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getJournalEntriesUseCase: GetJournalEntriesUseCase,
    private val hasMoodCheckInForDateUseCase: HasMoodCheckInForDateUseCase,
    private val getWeeklyMoodViewUseCase: GetWeeklyMoodViewUseCase,
    private val sessionManager: SessionManager,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

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
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            try {
                // Load session state
                observeSessionState()

                // Load mood check-in status
                checkMoodCheckInStatus()
                checkDashboardState()

                // Load data
                fetchJournalEntries()
                fetchWeeklyMoodView()

            } catch (e: Exception) {
                Timber.e(e, "Error during initialization")
            }
        }
    }

    private fun observeSessionState() {
        viewModelScope.launch {
            sessionManager.isGuest.collect { isGuest ->
                _uiState.value = _uiState.value.copy(isGuest = isGuest, isLoading = false)
                if (!isGuest) {
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
            val cachedProfile = sessionManager.getCachedUserProfile(userId)
            if (cachedProfile != null) {
                val userName = cachedProfile.firstName ?: cachedProfile.displayName ?: "User"
                _uiState.value = _uiState.value.copy(
                    userName = userName,
                    isPremium = cachedProfile.isPremium,
                    userProfileImageUrl = cachedProfile.profileImageUrl,
                    userLocalImagePath = cachedProfile.localImagePath,
                    userSelectedAvatarId = cachedProfile.selectedAvatarId,
                    userBackgroundColorHex = cachedProfile.backgroundColorHex
                )

                // Debug logging
                android.util.Log.d("HomeViewModel", "Loaded cached profile - ImageUrl: ${cachedProfile.profileImageUrl}, LocalPath: ${cachedProfile.localImagePath}, AvatarId: ${cachedProfile.selectedAvatarId}")
            } else {
                refreshUserProfileFromFirebase(userId)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading user profile data")
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
                    val profileImageUrl = document.getString("profileImageUrl")
                    val localImagePath = document.getString("localImagePath")
                    val selectedAvatarId = document.getString("selectedAvatarId")
                    val backgroundColorHex = document.getString("backgroundColorHex")
                    val userName = firstName ?: displayName ?: "User"
                    _uiState.value = _uiState.value.copy(
                        userName = userName,
                        isPremium = isPremium,
                        userProfileImageUrl = profileImageUrl,
                        userLocalImagePath = localImagePath,
                        userSelectedAvatarId = selectedAvatarId,
                        userBackgroundColorHex = backgroundColorHex
                    )

                    // Debug logging
                    android.util.Log.d("HomeViewModel", "Loaded from Firebase - ImageUrl: $profileImageUrl, LocalPath: $localImagePath, AvatarId: $selectedAvatarId")
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "Failed to fetch user profile from Firebase")
                    _uiState.value = _uiState.value.copy(userName = "User", isPremium = false)
                }
        } catch (e: Exception) {
            Timber.e(e, "Exception during Firebase profile fetch")
            _uiState.value = _uiState.value.copy(userName = "User", isPremium = false)
        }
    }

    private fun fetchJournalEntries() {
        viewModelScope.launch {
            try {
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
                        Timber.d("HomeViewModel: Fetched ${entries.size} journal entries")
                        _uiState.value = _uiState.value.copy(
                            journalEntries = entries,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                Timber.e(e, "Exception in fetchJournalEntries")
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "An error occurred",
                    isLoading = false
                )
            }
        }
    }

    fun retryFetch() {
        fetchJournalEntries()
    }

    private fun checkMoodCheckInStatus() {
        viewModelScope.launch {
            try {
                val userId = sessionManager.currentUserId.first()
                if (userId != null && !sessionManager.isGuest.first()) {
                    val today = java.util.Date()
                    val result = hasMoodCheckInForDateUseCase(userId, today)
                    when (result) {
                        is Response.Success -> {
                            val hasCheckedIn = result.data
                            _uiState.value = _uiState.value.copy(needsMoodCheckIn = !hasCheckedIn)
                        }
                        is Response.Failure -> {
                            Timber.e(result.exception, "Error checking mood check-in status")
                            _uiState.value = _uiState.value.copy(needsMoodCheckIn = false)
                        }
                        is Response.Loading -> { /* Optionally handle loading */ }
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
        viewModelScope.launch {
            try {
                val userId = sessionManager.currentUserId.first()
                if (userId != null && !sessionManager.isGuest.first()) {
                    val today = java.util.Date()
                    val result = hasMoodCheckInForDateUseCase(userId, today)
                    when (result) {
                        is Response.Success -> {
                            val hasCheckedIn = result.data
                            val newState = if (hasCheckedIn) DashboardState.Unfolded else DashboardState.Initial
                            _dashboardState.value = newState
                        }
                        is Response.Failure -> {
                            Timber.e(result.exception, "Error checking mood check-in for dashboard state")
                            _dashboardState.value = DashboardState.Initial
                        }
                        is Response.Loading -> {
                            // Keep current state while loading
                        }
                    }
                } else {
                    _dashboardState.value = DashboardState.Initial
                }
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error checking dashboard state")
                _dashboardState.value = DashboardState.Initial
            }
        }
    }

    fun onCheckInCompletedOrSkipped() {
        viewModelScope.launch {
            val userId = sessionManager.currentUserId.first()
            if (userId != null) {
                val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                sessionManager.cacheMoodCheckInCompleted(userId, today)
                _uiState.value = _uiState.value.copy(needsMoodCheckIn = false)
                _dashboardState.value = DashboardState.Unfolded
            }
            sessionManager.setLastCheckInTimestamp(System.currentTimeMillis())
            fetchWeeklyMoodView()
        }
    }

    fun refreshHomeData() {
        fetchJournalEntries()
        fetchWeeklyMoodView()
    }

    fun logOut(navController: NavController?) {
        viewModelScope.launch {
            try {
                val userId = sessionManager.currentUserId.first()
                if (userId != null) {
                    sessionManager.clearCachedUserProfile(userId)
                    sessionManager.clearCachedMoodCheckIn(userId)
                }
                auth.signOut()
                sessionManager.endGuestSession()
                navController?.navigate("sign_in") {
                    popUpTo("home_graph") { inclusive = true }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error during logout")
                auth.signOut()
                sessionManager.endGuestSession()
                navController?.navigate("sign_in") {
                    popUpTo("home_graph") { inclusive = true }
                }
            }
        }
    }

    /**
     * TEMPORARY DEBUG: Clear today's mood check-in to allow testing
     */
    fun debugClearTodaysMoodCheckIn() {
        viewModelScope.launch {
            try {
                val userId = sessionManager.currentUserId.first()
                if (userId != null) {
                    // Clear the cached mood check-in
                    sessionManager.clearCachedMoodCheckIn(userId)

                    // Update UI state to show check-in is needed
                    _uiState.value = _uiState.value.copy(needsMoodCheckIn = true)
                    _dashboardState.value = DashboardState.Initial

                    Timber.d("DEBUG: Cleared today's mood check-in for testing")
                }
            } catch (e: Exception) {
                Timber.e(e, "DEBUG: Error clearing mood check-in")
            }
        }
    }

    fun refreshUserProfile() {
        viewModelScope.launch {
            try {
                val userId = sessionManager.currentUserId.first()
                if (userId != null) {
                    android.util.Log.d("HomeViewModel", "Refreshing user profile data")
                    loadUserProfileData(userId)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error refreshing user profile")
            }
        }
    }

    private fun fetchWeeklyMoodView() {
        viewModelScope.launch {
            try {
                getWeeklyMoodViewUseCase()
                    .catch { e ->
                        Timber.e(e, "Error fetching weekly mood view")
                    }
                    .collect { weeklyView ->
                        Timber.d("HomeViewModel: Fetched weekly mood view with ${weeklyView.days.size} entries")
                        _uiState.value = _uiState.value.copy(weeklyMoodView = weeklyView)
                    }
            } catch (e: Exception) {
                Timber.e(e, "Exception in fetchWeeklyMoodView")
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
