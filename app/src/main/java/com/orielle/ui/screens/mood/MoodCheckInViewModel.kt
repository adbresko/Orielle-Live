package com.orielle.ui.screens.mood

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.manager.SessionManager
import com.orielle.domain.model.MoodCheckIn
import com.orielle.domain.use_case.HasMoodCheckInForDateUseCase
import com.orielle.domain.use_case.SaveMoodCheckInUseCase
import com.orielle.ui.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject

data class MoodCheckInUiState(
    val isLoading: Boolean = false,
    val userName: String? = null,
    val error: String? = null
)

@HiltViewModel
class MoodCheckInViewModel @Inject constructor(
    private val saveMoodCheckInUseCase: SaveMoodCheckInUseCase,
    private val hasMoodCheckInForDateUseCase: HasMoodCheckInForDateUseCase,
    private val sessionManager: SessionManager,
    private val firestore: com.google.firebase.firestore.FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(MoodCheckInUiState())
    val uiState: StateFlow<MoodCheckInUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            try {
                val userId = sessionManager.currentUserId.first()
                if (userId != null) {
                    firestore.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            val firstName = document.getString("firstName")
                            val displayName = document.getString("displayName")

                            // Use firstName for personalized greetings, fallback to displayName, then "User"
                            val userName = firstName ?: displayName ?: "User"
                            _uiState.value = _uiState.value.copy(userName = userName)
                        }
                        .addOnFailureListener { e ->
                            Timber.e(e, "Failed to fetch user profile")
                            _uiState.value = _uiState.value.copy(userName = "User")
                        }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading user data")
                _uiState.value = _uiState.value.copy(error = "Failed to load user data", userName = "User")
            }
        }
    }

    fun saveMoodCheckIn(mood: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val userId = sessionManager.currentUserId.first()
                if (userId == null) {
                    _eventFlow.emit(UiEvent.ShowSnackbar("User not authenticated"))
                    return@launch
                }

                val moodCheckIn = MoodCheckIn(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    mood = mood,
                    timestamp = Date()
                )

                val result = saveMoodCheckInUseCase(moodCheckIn)
                when (result) {
                    is com.orielle.domain.model.Response.Success -> {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Mood check-in saved!"))
                    }
                    is com.orielle.domain.model.Response.Failure -> {
                        _eventFlow.emit(UiEvent.ShowSnackbar("Failed to save mood check-in"))
                        Timber.e(result.exception, "Error saving mood check-in")
                    }
                    is com.orielle.domain.model.Response.Loading -> { /* Optionally handle loading */ }
                }
            } catch (e: Exception) {
                Timber.e(e, "Unexpected error saving mood check-in")
                _eventFlow.emit(UiEvent.ShowSnackbar("An unexpected error occurred"))
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    suspend fun checkIfNeedsMoodCheckIn(): Boolean {
        return try {
            val userId = sessionManager.currentUserId.first()
            if (userId == null) {
                return false
            }

            val today = Date()
            val result = hasMoodCheckInForDateUseCase(userId, today)

            when (result) {
                is com.orielle.domain.model.Response.Success -> !result.data
                is com.orielle.domain.model.Response.Failure -> {
                    Timber.e(result.exception, "Error checking mood check-in status")
                    false // Default to not showing mood check-in if there's an error
                }
                is com.orielle.domain.model.Response.Loading -> false
            }
        } catch (e: Exception) {
            Timber.e(e, "Unexpected error checking mood check-in status")
            false
        }
    }
}