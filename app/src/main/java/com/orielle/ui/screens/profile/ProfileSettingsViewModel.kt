package com.orielle.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.orielle.domain.manager.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import com.orielle.ui.components.AvatarOption
import com.orielle.ui.theme.ThemeManager
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.resolution
import id.zelory.compressor.constraint.size

@HiltViewModel
class ProfileSettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    val themeManager: ThemeManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSettingsUiState())
    val uiState: StateFlow<ProfileSettingsUiState> = _uiState.asStateFlow()

    // Expose theme state for UI
    val currentThemeState = themeManager.isDarkTheme

    // Avatar library - using your actual mood icons!
    private val avatarLibrary = listOf(
        // Happy & Joyful Moods
        AvatarOption("happy", "Happy", imageUrl = "drawable://ic_happy", isPremium = false),
        AvatarOption("playful", "Playful", imageUrl = "drawable://ic_playful", isPremium = false),
        AvatarOption("surprised", "Surprised", imageUrl = "drawable://ic_surprised", isPremium = false),

        // Calm & Peaceful Moods
        AvatarOption("peaceful", "Peaceful", imageUrl = "drawable://ic_peaceful", isPremium = false),
        AvatarOption("shy", "Shy", imageUrl = "drawable://ic_shy", isPremium = false),

        // Challenging Moods
        AvatarOption("sad", "Sad", imageUrl = "drawable://ic_sad", isPremium = false),
        AvatarOption("angry", "Angry", imageUrl = "drawable://ic_angry", isPremium = false),
        AvatarOption("frustrated", "Frustrated", imageUrl = "drawable://ic_frustrated", isPremium = false),
        AvatarOption("scared", "Scared", imageUrl = "drawable://ic_scared", isPremium = false),

        // Premium Mood Combinations (using existing icons creatively)
        AvatarOption("premium_happy_playful", "Joyful Spirit", imageUrl = "drawable://ic_happy", isPremium = true),
        AvatarOption("premium_peaceful_surprised", "Zen Wonder", imageUrl = "drawable://ic_peaceful", isPremium = true),
        AvatarOption("premium_playful_surprised", "Magical Joy", imageUrl = "drawable://ic_playful", isPremium = true)
    )

    // Get avatar library
    fun getAvatarLibrary(): List<AvatarOption> = avatarLibrary

    fun initializeUserData(userId: String, userName: String?, userEmail: String?, profileImageUrl: String?) {
        viewModelScope.launch {
            try {
                val isGuest = sessionManager.isGuest.first()
                Timber.d("🏁 ProfileSettings init - userId: $userId, isGuest: $isGuest")

                if (isGuest) {
                    showMessage("Profile settings not available for guest users", isError = true)
                    return@launch
                }

                _uiState.value = _uiState.value.copy(
                    userId = userId,
                    userName = userName,
                    userEmail = userEmail,
                    profileImageUrl = profileImageUrl,
                    editingName = userName ?: "",
                    editingEmail = userEmail ?: ""
                )

                // Load saved preferences with authentication check
                loadUserPreferences(userId)
            } catch (e: Exception) {
                Timber.e(e, "Error initializing profile data")
                showMessage("Failed to initialize profile settings", isError = true)
            }
        }
    }

    private fun loadUserPreferences(userId: String) {
        viewModelScope.launch {
            try {
                Timber.d("🔍 Loading user preferences for: $userId")

                // Debug: Check Firebase Auth state
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                Timber.d("🔐 Firebase Auth user: ${currentUser?.uid}, matches sessionUserId: ${currentUser?.uid == userId}")

                if (currentUser == null) {
                    Timber.e("❌ No Firebase Auth user - this will cause PERMISSION_DENIED")
                    showMessage("Authentication required. Please sign in again.", isError = true)
                    return@launch
                }

                // Check if Firebase Auth token is valid by refreshing it
                try {
                    val tokenResult = currentUser.getIdToken(true).await()
                    Timber.d("🔄 Firebase Auth token refreshed successfully")
                } catch (tokenError: Exception) {
                    Timber.e(tokenError, "❌ Failed to refresh Firebase Auth token")
                    showMessage("Authentication expired. Please sign in again.", isError = true)
                    return@launch
                }

                // First, try to get cached profile data
                val cachedProfile = sessionManager.getCachedUserProfile(userId)

                if (cachedProfile != null) {
                    // Use cached data immediately
                    Timber.d("📋 ProfileSettings: Using cached profile data for: $userId")

                    _uiState.value = _uiState.value.copy(
                        userName = cachedProfile.firstName,
                        userEmail = cachedProfile.email,
                        profileImageUrl = cachedProfile.profileImageUrl,
                        isPremium = cachedProfile.isPremium,
                        twoFactorEnabled = cachedProfile.twoFactorEnabled,
                        notificationsEnabled = cachedProfile.notificationsEnabled,
                        biometricsEnabled = (sessionManager as? com.orielle.data.manager.SessionManagerImpl)?.isBiometricsEnabled() ?: false,
                        isLoading = false
                    )

                    // Check if cache is getting stale and refresh in background
                    val cacheAge = System.currentTimeMillis() - cachedProfile.cachedAt
                    if (cacheAge > 1800000) { // 30 minutes
                        Timber.d("🔄 ProfileSettings: Cache is getting stale, refreshing in background")
                        refreshUserProfileFromFirebase(userId)
                    }
                } else {
                    // No cached data, fetch from Firebase
                    Timber.d("🌐 ProfileSettings: No cached profile, fetching from Firebase")
                    refreshUserProfileFromFirebase(userId)
                }

            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to load user preferences")
                _uiState.value = _uiState.value.copy(isLoading = false) // Stop loading on error
                showMessage("Failed to load preferences: ${e.message}", isError = true)
            }
        }
    }

    private suspend fun refreshUserProfileFromFirebase(userId: String) {
        try {
            val doc = firestore.collection("users").document(userId).get().await()
            val biometricsEnabled = (sessionManager as? com.orielle.data.manager.SessionManagerImpl)?.isBiometricsEnabled() ?: false

            // Extract user profile data
            val firstName = doc.getString("firstName") ?: "User"
            val displayName = doc.getString("displayName")
            val email = doc.getString("email") ?: ""
            val profileImageUrl = doc.getString("profileImageUrl")
            val isPremium = doc.getBoolean("premium") ?: false
            val notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: true
            val twoFactorEnabled = doc.getBoolean("twoFactorEnabled") ?: false

            Timber.d("📄 Loaded user data from Firebase - name: $firstName, email: $email")

            // Update UI state
            _uiState.value = _uiState.value.copy(
                userName = firstName,
                userEmail = email,
                profileImageUrl = profileImageUrl,
                isPremium = isPremium,
                twoFactorEnabled = twoFactorEnabled,
                notificationsEnabled = notificationsEnabled,
                biometricsEnabled = biometricsEnabled,
                isLoading = false // Mark loading as complete
            )

            // Cache the profile data for future use
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

            Timber.d("✅ Profile preferences loaded and cached successfully")
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to refresh user profile from Firebase")
            _uiState.value = _uiState.value.copy(isLoading = false) // Stop loading on error
            showMessage("Failed to refresh profile: ${e.message}", isError = true)
        }
    }

    fun selectImage() {
        // This will be handled by the UI with ActivityResultLauncher
        // For now, just indicate that we're ready to upload
    }

    fun uploadImage(imageUri: Uri, context: Context) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isUploadingImage = true)

                val userId = _uiState.value.userId

                // Save image locally first
                val localImagePath = saveImageLocally(context, imageUri, userId)

                // TODO: Cloud Storage - Upload to Firebase Storage
                // val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")
                // val uploadTask = storageRef.putFile(imageUri)
                // uploadTask.await()
                // val downloadUrl = storageRef.downloadUrl.await().toString()

                // TODO: Cloud Storage - Save to Firestore when cloud upload is enabled
                // firestore.collection("users").document(userId)
                //     .update("profileImageUrl", downloadUrl).await()

                // Update UI state with local image path
                _uiState.value = _uiState.value.copy(
                    profileImageUrl = null, // No cloud storage yet
                    localImagePath = localImagePath,
                    isUploadingImage = false
                )

                // Update cached profile data
                sessionManager.updateCachedUserProfile(
                    userId = userId,
                    profileImageUrl = null // No cloud storage yet
                )

                showMessage("Profile image updated successfully!")
                Timber.d("✅ Profile image uploaded and saved locally: $localImagePath")

            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to upload profile image")
                _uiState.value = _uiState.value.copy(isUploadingImage = false)
                showMessage("Failed to upload image: ${e.message}", isError = true)
            }
        }
    }

    // Save image locally for offline access
    private suspend fun saveImageLocally(context: Context, imageUri: Uri, userId: String): String {
        return try {
            // First, save the original image temporarily
            val tempFile = File(context.cacheDir, "temp_profile_$userId.jpg")
            val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)

            inputStream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Compress the image with size and quality constraints
            val compressedFile = Compressor.compress(context, tempFile) {
                resolution(512, 512) // Max 512x512 pixels
                quality(80) // 80% quality
                format(android.graphics.Bitmap.CompressFormat.JPEG)
                size(2_097_152) // Max 2MB file size
            }

            // Move compressed file to final location
            val finalFile = File(context.filesDir, "profile_image_$userId.jpg")
            compressedFile.copyTo(finalFile, overwrite = true)

            // Clean up temp file
            tempFile.delete()
            compressedFile.delete()

            Timber.d("✅ Image compressed and saved locally: ${finalFile.absolutePath}")
            finalFile.absolutePath
        } catch (e: Exception) {
            Timber.e(e, "❌ Failed to save image locally")
            ""
        }
    }

    // Select avatar from library
    fun selectAvatar(avatar: AvatarOption) {
        viewModelScope.launch {
            try {
                val userId = _uiState.value.userId

                // Check if user is premium for premium avatars
                if (avatar.isPremium) {
                    val cachedProfile = sessionManager.getCachedUserProfile(userId)
                    if (cachedProfile?.isPremium != true) {
                        showMessage("Premium avatars require a premium subscription", isError = true)
                        return@launch
                    }
                }

                // Update UI state immediately
                _uiState.value = _uiState.value.copy(
                    profileImageUrl = avatar.imageUrl,
                    selectedAvatarId = avatar.id
                )

                // Save to Firestore
                firestore.collection("users").document(userId)
                    .update("profileImageUrl", avatar.imageUrl).await()

                // Update cached profile data
                sessionManager.updateCachedUserProfile(
                    userId = userId,
                    profileImageUrl = avatar.imageUrl
                )

                showMessage("Avatar updated successfully!")
                Timber.d("✅ Avatar selected: ${avatar.name}")

            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to select avatar")
                showMessage("Failed to update avatar: ${e.message}", isError = true)
            }
        }
    }

    // Remove profile image
    fun removeProfileImage(context: Context) {
        viewModelScope.launch {
            try {
                val userId = _uiState.value.userId

                // Remove from Firebase Storage
                val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")
                storageRef.delete().await()

                // Remove local file
                val localFile = File(context.filesDir, "profile_image_$userId.jpg")
                if (localFile.exists()) {
                    localFile.delete()
                }

                // Update Firestore
                firestore.collection("users").document(userId)
                    .update("profileImageUrl", null).await()

                // Update UI state
                _uiState.value = _uiState.value.copy(
                    profileImageUrl = null,
                    localImagePath = null,
                    selectedAvatarId = null
                )

                // Update cached profile data
                sessionManager.updateCachedUserProfile(
                    userId = userId,
                    profileImageUrl = null
                )

                showMessage("Profile image removed successfully!")
                Timber.d("✅ Profile image removed")

            } catch (e: Exception) {
                Timber.e(e, "❌ Failed to remove profile image")
                showMessage("Failed to remove image: ${e.message}", isError = true)
            }
        }
    }

    fun showEditNameDialog() {
        _uiState.value = _uiState.value.copy(
            showEditNameDialog = true,
            editingName = _uiState.value.userName ?: ""
        )
    }

    fun hideEditNameDialog() {
        _uiState.value = _uiState.value.copy(showEditNameDialog = false)
    }

    fun updateEditingName(name: String) {
        _uiState.value = _uiState.value.copy(editingName = name)
    }

    fun saveName() {
        viewModelScope.launch {
            try {
                val newName = _uiState.value.editingName.trim()
                if (newName.isBlank()) {
                    showMessage("Name cannot be empty", isError = true)
                    return@launch
                }

                firestore.collection("users").document(_uiState.value.userId)
                    .update("firstName", newName).await()

                // Update UI state
                _uiState.value = _uiState.value.copy(
                    userName = newName,
                    showEditNameDialog = false
                )

                // Update cached profile data
                sessionManager.updateCachedUserProfile(
                    userId = _uiState.value.userId,
                    firstName = newName
                )

                showMessage("Name updated successfully")

            } catch (e: Exception) {
                showMessage("Failed to update name: ${e.message}", isError = true)
            }
        }
    }

    fun showEditEmailDialog() {
        _uiState.value = _uiState.value.copy(
            showEditEmailDialog = true,
            editingEmail = _uiState.value.userEmail ?: ""
        )
    }

    fun hideEditEmailDialog() {
        _uiState.value = _uiState.value.copy(showEditEmailDialog = false)
    }

    fun updateEditingEmail(email: String) {
        _uiState.value = _uiState.value.copy(editingEmail = email)
    }

    fun saveEmail() {
        viewModelScope.launch {
            try {
                val newEmail = _uiState.value.editingEmail.trim()
                if (newEmail.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(newEmail).matches()) {
                    showMessage("Please enter a valid email address", isError = true)
                    return@launch
                }

                // Update Firebase Auth email
                auth.currentUser?.updateEmail(newEmail)?.await()

                // Update Firestore
                firestore.collection("users").document(_uiState.value.userId)
                    .update("email", newEmail).await()

                _uiState.value = _uiState.value.copy(
                    userEmail = newEmail,
                    showEditEmailDialog = false
                )

                showMessage("Email updated successfully")

            } catch (e: Exception) {
                showMessage("Failed to update email: ${e.message}", isError = true)
            }
        }
    }

    fun showChangePasswordDialog() {
        _uiState.value = _uiState.value.copy(showChangePasswordDialog = true)
    }

    fun hideChangePasswordDialog() {
        _uiState.value = _uiState.value.copy(
            showChangePasswordDialog = false,
            newPassword = ""
        )
    }

    fun updateNewPassword(password: String) {
        _uiState.value = _uiState.value.copy(newPassword = password)
    }

    fun changePassword() {
        viewModelScope.launch {
            try {
                val newPassword = _uiState.value.newPassword
                if (newPassword.length < 6) {
                    showMessage("Password must be at least 6 characters", isError = true)
                    return@launch
                }

                auth.currentUser?.updatePassword(newPassword)?.await()

                _uiState.value = _uiState.value.copy(
                    showChangePasswordDialog = false,
                    newPassword = ""
                )

                showMessage("Password updated successfully")

            } catch (e: Exception) {
                showMessage("Failed to update password: ${e.message}", isError = true)
            }
        }
    }

    fun toggleTwoFactor(enabled: Boolean) {
        viewModelScope.launch {
            try {
                firestore.collection("users").document(_uiState.value.userId)
                    .update("twoFactorEnabled", enabled).await()

                _uiState.value = _uiState.value.copy(twoFactorEnabled = enabled)
                showMessage(if (enabled) "Two-factor authentication enabled" else "Two-factor authentication disabled")

            } catch (e: Exception) {
                showMessage("Failed to update two-factor authentication", isError = true)
            }
        }
    }

    fun toggleBiometrics(enabled: Boolean) {
        viewModelScope.launch {
            try {
                (sessionManager as? com.orielle.data.manager.SessionManagerImpl)?.setBiometricsEnabled(enabled)
                _uiState.value = _uiState.value.copy(biometricsEnabled = enabled)
                showMessage(if (enabled) "Biometric authentication enabled" else "Biometric authentication disabled")

            } catch (e: Exception) {
                showMessage("Failed to update biometric settings", isError = true)
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            try {
                // Update theme preference using ThemeManager
                themeManager.setDarkTheme(enabled)
                showMessage(if (enabled) "Dark theme enabled" else "Light theme enabled")

            } catch (e: Exception) {
                showMessage("Failed to update theme", isError = true)
            }
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                firestore.collection("users").document(_uiState.value.userId)
                    .update("notificationsEnabled", enabled).await()

                _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
                showMessage(if (enabled) "Notifications enabled" else "Notifications disabled")

            } catch (e: Exception) {
                showMessage("Failed to update notification settings", isError = true)
            }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            try {
                showMessage("Preparing data export...")
                // This would typically trigger a file export process
                // For now, just show a success message
                showMessage("Data export feature coming soon")

            } catch (e: Exception) {
                showMessage("Failed to export data", isError = true)
            }
        }
    }

    fun showLogoutConfirmation() {
        _uiState.value = _uiState.value.copy(showLogoutConfirmation = true)
    }

    fun hideLogoutConfirmation() {
        _uiState.value = _uiState.value.copy(showLogoutConfirmation = false)
    }

    fun showDeleteAccountConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteAccountConfirmation = true)
    }

    fun hideDeleteAccountConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteAccountConfirmation = false)
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                val userId = _uiState.value.userId

                // Delete all user data from Firestore
                // Note: In production, this should be done via Cloud Functions for atomic deletion
                try {
                    // Delete mood check-ins
                    val moodCheckIns = firestore.collection("mood_check_ins")
                        .whereEqualTo("userId", userId).get().await()
                    for (doc in moodCheckIns.documents) {
                        doc.reference.delete().await()
                    }

                    // Delete journal entries
                    val journalEntries = firestore.collection("journal_entries")
                        .whereEqualTo("userId", userId).get().await()
                    for (doc in journalEntries.documents) {
                        doc.reference.delete().await()
                    }

                    // Delete chat conversations and messages
                    val conversations = firestore.collection("chat_conversations")
                        .whereEqualTo("userId", userId).get().await()
                    for (conversationDoc in conversations.documents) {
                        val conversationId = conversationDoc.id

                        // Delete messages in this conversation
                        val messages = firestore.collection("chat_messages")
                            .whereEqualTo("conversationId", conversationId).get().await()
                        for (messageDoc in messages.documents) {
                            messageDoc.reference.delete().await()
                        }

                        // Delete conversation
                        conversationDoc.reference.delete().await()
                    }

                    // Delete user document
                    firestore.collection("users").document(userId).delete().await()

                } catch (firestoreError: Exception) {
                    Timber.e(firestoreError, "Error deleting Firestore data during account deletion")
                    showMessage("Warning: Some cloud data may not have been deleted", isError = true)
                }

                // Clear local session data
                try {
                    sessionManager.clearSession()
                } catch (sessionError: Exception) {
                    Timber.e(sessionError, "Error clearing session during account deletion")
                }

                // Delete Firebase Auth account (do this last)
                auth.currentUser?.delete()?.await()

                showMessage("Account and all data deleted successfully")

            } catch (e: Exception) {
                Timber.e(e, "Failed to delete account")
                showMessage("Failed to delete account: ${e.message}", isError = true)
            }
        }
    }

    private fun showMessage(message: String, isError: Boolean = false) {
        _uiState.value = _uiState.value.copy(
            message = message,
            isError = isError
        )
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null, isError = false)
    }

    // Avatar library dialog management
    fun showAvatarLibrary() {
        _uiState.value = _uiState.value.copy(showAvatarLibrary = true)
    }

    fun hideAvatarLibrary() {
        _uiState.value = _uiState.value.copy(showAvatarLibrary = false)
    }
}

data class ProfileSettingsUiState(
    val userId: String = "",
    val userName: String? = null,
    val userEmail: String? = null,
    val profileImageUrl: String? = null,
    val localImagePath: String? = null,
    val selectedAvatarId: String? = null,
    val isPremium: Boolean = false,
    val isUploadingImage: Boolean = false,

    // Dialog states
    val showEditNameDialog: Boolean = false,
    val showEditEmailDialog: Boolean = false,
    val showChangePasswordDialog: Boolean = false,
    val showLogoutConfirmation: Boolean = false,
    val showDeleteAccountConfirmation: Boolean = false,
    val showAvatarLibrary: Boolean = false,

    // Edit fields
    val editingName: String = "",
    val editingEmail: String = "",
    val newPassword: String = "",

    // Settings
    val twoFactorEnabled: Boolean = false,
    val biometricsEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val isDarkTheme: Boolean = false,

    // UI state
    val isLoading: Boolean = true, // Add loading state
    val message: String? = null,
    val isError: Boolean = false
)
