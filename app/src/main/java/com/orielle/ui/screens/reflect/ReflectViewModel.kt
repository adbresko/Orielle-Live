package com.orielle.ui.screens.reflect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.data.repository.JournalPromptRepository
import com.orielle.data.manager.DailyContentManager
import com.orielle.domain.model.JournalEntry
import com.orielle.domain.use_case.GetJournalEntriesUseCase
import com.orielle.domain.use_case.HasMoodCheckInForDateUseCase
import com.orielle.domain.use_case.GetMoodCheckInForDateUseCase
import com.orielle.domain.manager.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

@HiltViewModel
class ReflectViewModel @Inject constructor(
    private val getJournalEntriesUseCase: GetJournalEntriesUseCase,
    private val sessionManager: SessionManager,
    private val journalPromptRepository: JournalPromptRepository,
    private val dailyContentManager: DailyContentManager,
    private val hasMoodCheckInForDateUseCase: HasMoodCheckInForDateUseCase,
    private val getMoodCheckInForDateUseCase: GetMoodCheckInForDateUseCase,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReflectUiState())
    val uiState: StateFlow<ReflectUiState> = _uiState.asStateFlow()

    init {
        initializeData()
    }

    private fun initializeData() {
        viewModelScope.launch {
            try {
                // Load profile data immediately (synchronous)
                loadProfileDataImmediately()

                // Load session state
                observeSessionState()

                // Initialize prompts and check mood status
                initializePrompts()
                checkMoodCheckInStatus()

            } catch (e: Exception) {
                Timber.e(e, "❌ ReflectViewModel: Error in initialization")
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
                    android.util.Log.d("ReflectViewModel", "✅ Loaded profile immediately - userName: $userName, AvatarId: ${cachedProfile.selectedAvatarId}")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading profile data immediately")
        }
    }

    private fun observeSessionState() {
        viewModelScope.launch {
            sessionManager.isGuest.collect { isGuest ->
                android.util.Log.d("ReflectViewModel", "🔍 observeSessionState - isGuest: $isGuest")
                _uiState.value = _uiState.value.copy(isLoading = false)
                if (!isGuest) {
                    val userId = sessionManager.currentUserId.first()
                    android.util.Log.d("ReflectViewModel", "🔍 Not guest, userId: $userId")
                    if (userId != null) {
                        loadUserProfileData(userId)
                    }
                } else {
                    android.util.Log.d("ReflectViewModel", "🔍 Is guest, setting userName to null")
                    _uiState.value = _uiState.value.copy(userName = null)
                }
            }
        }
    }

    private suspend fun initializePrompts() {
        try {
            // Force reload prompts to ensure we have the latest CSV data
            journalPromptRepository.reloadPrompts()
        } catch (e: Exception) {
            Timber.e(e, "❌ ReflectViewModel: Failed to initialize journal prompts")
            _uiState.value = _uiState.value.copy(error = "Failed to load prompts")
        }
    }

    private suspend fun checkMoodCheckInStatus() {
        try {
            val userId = sessionManager.currentUserId.first()
            if (userId != null) {
                val today = Date()
                val hasCheckIn = hasMoodCheckInForDateUseCase(userId, today)

                if (hasCheckIn is com.orielle.domain.model.Response.Success && hasCheckIn.data) {
                    // State 2: Has check-in - load personalized prompt
                    loadPersonalizedPrompt(userId, today)
                } else {
                    // State 1: No check-in - show invitation state
                    _uiState.value = _uiState.value.copy(
                        hasMoodCheckIn = false,
                        isLoading = false
                    )
                }
            } else {
                // Guest user - show invitation state
                _uiState.value = _uiState.value.copy(
                    hasMoodCheckIn = false,
                    isLoading = false
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ ReflectViewModel: Failed to check mood check-in status")
            _uiState.value = _uiState.value.copy(
                hasMoodCheckIn = false,
                isLoading = false,
                error = "Failed to check mood status"
            )
        }
    }

    private suspend fun loadPersonalizedPrompt(userId: String, date: Date) {
        try {
            Timber.d("🔍 ReflectViewModel: Loading personalized prompt for user: $userId, date: $date")
            val moodCheckInResponse = getMoodCheckInForDateUseCase(userId, date)
            if (moodCheckInResponse is com.orielle.domain.model.Response.Success) {
                val moodCheckIn = moodCheckInResponse.data
                if (moodCheckIn != null) {
                    Timber.d("🎯 ReflectViewModel: Found mood check-in with mood: '${moodCheckIn.mood}'")
                    // Get today's prompt for this mood (same prompt all day)
                    val todaysPrompt = dailyContentManager.getTodaysPrompt(moodCheckIn.mood)
                    Timber.d("📝 ReflectViewModel: Retrieved today's prompt for mood '${moodCheckIn.mood}': ${todaysPrompt ?: "null"}")
                    if (todaysPrompt != null) {
                        _uiState.value = _uiState.value.copy(
                            hasMoodCheckIn = true,
                            todaysPrompt = todaysPrompt,
                            todaysMood = moodCheckIn.mood,
                            isLoading = false
                        )
                        Timber.d("✅ ReflectViewModel: Set today's prompt: '$todaysPrompt'")
                    } else {
                        // Fallback if no prompt found for mood
                        Timber.w("⚠️ ReflectViewModel: No prompt found for mood '${moodCheckIn.mood}', using fallback")
                        _uiState.value = _uiState.value.copy(
                            hasMoodCheckIn = true,
                            todaysPrompt = "What is one thing on your mind today?",
                            todaysMood = moodCheckIn.mood,
                            isLoading = false
                        )
                    }
                } else {
                    Timber.w("⚠️ ReflectViewModel: No mood check-in found for date")
                }
            } else {
                Timber.e("❌ ReflectViewModel: Failed to get mood check-in: ${moodCheckInResponse}")
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ ReflectViewModel: Failed to load personalized prompt")
            _uiState.value = _uiState.value.copy(
                hasMoodCheckIn = false,
                isLoading = false,
                error = "Failed to load prompt"
            )
        }
    }

    private suspend fun loadUserProfileData(userId: String) {
        try {
            android.util.Log.d("ReflectViewModel", "🔍 Loading profile data for userId: $userId")
            val cachedProfile = sessionManager.getCachedUserProfile(userId)
            android.util.Log.d("ReflectViewModel", "🔍 Cached profile: $cachedProfile")
            if (cachedProfile != null) {
                val userName = cachedProfile.firstName ?: cachedProfile.displayName ?: "User"
                _uiState.value = _uiState.value.copy(
                    userName = userName,
                    userProfileImageUrl = cachedProfile.profileImageUrl,
                    userLocalImagePath = cachedProfile.localImagePath,
                    userSelectedAvatarId = cachedProfile.selectedAvatarId,
                    userBackgroundColorHex = cachedProfile.backgroundColorHex
                )
                android.util.Log.d("ReflectViewModel", "🔍 Updated uiState - userName: ${_uiState.value.userName}, AvatarId: ${_uiState.value.userSelectedAvatarId}")
            } else {
                android.util.Log.d("ReflectViewModel", "🔍 No cached profile found, loading from Firebase")
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
                    android.util.Log.d("ReflectViewModel", "Loaded from Firebase - ImageUrl: $profileImageUrl, LocalPath: $localImagePath, AvatarId: $selectedAvatarId, UserName: $userName")
                }
                .addOnFailureListener { e ->
                    Timber.e(e, "Failed to fetch user profile from Firebase")
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
                    android.util.Log.d("ReflectViewModel", "Refreshing user profile data")
                    loadUserProfileData(userId)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error refreshing user profile")
            }
        }
    }
}

data class ReflectUiState(
    val todaysPrompt: String = "",
    val todaysMood: String? = null,
    val hasMoodCheckIn: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    // Profile data for avatar display
    val userName: String? = null,
    val userProfileImageUrl: String? = null,
    val userLocalImagePath: String? = null,
    val userSelectedAvatarId: String? = null,
    val userBackgroundColorHex: String? = null
)
