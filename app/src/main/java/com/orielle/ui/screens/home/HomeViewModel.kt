package com.orielle.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.manager.SessionManager
import com.orielle.domain.model.JournalEntry
import com.orielle.domain.use_case.GetJournalEntriesUseCase
import com.orielle.domain.use_case.HasMoodCheckInForDateUseCase
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

data class HomeUiState(
    val isGuest: Boolean = true,
    val isLoading: Boolean = true,
    val journalEntries: List<JournalEntry> = emptyList(),
    val error: String? = null,
    val userName: String? = null, // Added userName property
    val isPremium: Boolean = false, // Added isPremium property
    val needsMoodCheckIn: Boolean = false // Added mood check-in status
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getJournalEntriesUseCase: GetJournalEntriesUseCase,
    private val hasMoodCheckInForDateUseCase: HasMoodCheckInForDateUseCase,
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

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable, "Unhandled coroutine exception in HomeViewModel")
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar("Unexpected error: ${throwable.localizedMessage ?: "Unknown error"}"))
        }
    }

    init {
        observeSessionState()
        fetchJournalEntries()
        checkMoodCheckInStatus()
    }

    private fun observeSessionState() {
        viewModelScope.launch(coroutineExceptionHandler) {
            _uiState.value = _uiState.value.copy(isLoading = true)
            sessionManager.isGuest.collect { isGuest ->
                _uiState.value = _uiState.value.copy(isGuest = isGuest, isLoading = false)
                if (!isGuest) {
                    // Fetch user name and premium status from Firestore
                    val userId = sessionManager.currentUserId.first()
                    if (userId != null) {
                        firestore.collection("users").document(userId).get()
                            .addOnSuccessListener { document ->
                                val displayName = document.getString("displayName")
                                val isPremium = document.getBoolean("premium") == true
                                _uiState.value = _uiState.value.copy(userName = displayName, isPremium = isPremium)
                            }
                            .addOnFailureListener { e ->
                                Timber.e(e, "Failed to fetch user profile")
                            }
                    }
                } else {
                    _uiState.value = _uiState.value.copy(userName = null, isPremium = false)
                }
            }
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
                if (userId != null) {
                    val today = java.util.Date()
                    val result = hasMoodCheckInForDateUseCase(userId, today)

                    when (result) {
                        is com.orielle.domain.model.Response.Success -> {
                            _uiState.value = _uiState.value.copy(needsMoodCheckIn = !result.data)
                        }
                        is com.orielle.domain.model.Response.Failure -> {
                            Timber.e(result.exception, "Error checking mood check-in status")
                        }
                        is com.orielle.domain.model.Response.Loading -> { /* Optionally handle loading */ }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error checking mood check-in status")
            }
        }
    }

    fun logOut() {
        viewModelScope.launch {
            auth.signOut()
            sessionManager.endGuestSession()
            _logOutEvent.emit(Unit)
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