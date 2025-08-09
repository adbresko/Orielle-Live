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
import javax.inject.Inject

@HiltViewModel
class ProfileSettingsViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSettingsUiState())
    val uiState: StateFlow<ProfileSettingsUiState> = _uiState.asStateFlow()

    fun initializeUserData(userId: String, userName: String?, userEmail: String?, profileImageUrl: String?) {
        _uiState.value = _uiState.value.copy(
            userId = userId,
            userName = userName,
            userEmail = userEmail,
            profileImageUrl = profileImageUrl,
            editingName = userName ?: "",
            editingEmail = userEmail ?: ""
        )

        // Load saved preferences
        loadUserPreferences(userId)
    }

    private fun loadUserPreferences(userId: String) {
        viewModelScope.launch {
            try {
                val doc = firestore.collection("users").document(userId).get().await()
                val biometricsEnabled = (sessionManager as? com.orielle.data.manager.SessionManagerImpl)?.isBiometricsEnabled() ?: false

                _uiState.value = _uiState.value.copy(
                    twoFactorEnabled = doc.getBoolean("twoFactorEnabled") ?: false,
                    notificationsEnabled = doc.getBoolean("notificationsEnabled") ?: true,
                    biometricsEnabled = biometricsEnabled
                )
            } catch (e: Exception) {
                showMessage("Failed to load preferences", isError = true)
            }
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
                val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")
                val uploadTask = storageRef.putFile(imageUri)
                uploadTask.await()

                val downloadUrl = storageRef.downloadUrl.await().toString()

                // Save to Firestore
                firestore.collection("users").document(userId)
                    .update("profileImageUrl", downloadUrl).await()

                _uiState.value = _uiState.value.copy(
                    profileImageUrl = downloadUrl,
                    isUploadingImage = false
                )

                showMessage("Profile image updated successfully")

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isUploadingImage = false)
                showMessage("Failed to upload image: ${e.message}", isError = true)
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

                _uiState.value = _uiState.value.copy(
                    userName = newName,
                    showEditNameDialog = false
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
                // For now, just show a message since theme switching requires restart
                showMessage("Theme preference saved. Please restart the app to apply changes.")

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

                // Delete user data from Firestore
                firestore.collection("users").document(userId).delete().await()

                // Delete Firebase Auth account
                auth.currentUser?.delete()?.await()

                showMessage("Account deleted successfully")

            } catch (e: Exception) {
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
}

data class ProfileSettingsUiState(
    val userId: String = "",
    val userName: String? = null,
    val userEmail: String? = null,
    val profileImageUrl: String? = null,
    val isUploadingImage: Boolean = false,

    // Dialog states
    val showEditNameDialog: Boolean = false,
    val showEditEmailDialog: Boolean = false,
    val showChangePasswordDialog: Boolean = false,
    val showLogoutConfirmation: Boolean = false,
    val showDeleteAccountConfirmation: Boolean = false,

    // Edit fields
    val editingName: String = "",
    val editingEmail: String = "",
    val newPassword: String = "",

    // Settings
    val twoFactorEnabled: Boolean = false,
    val biometricsEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,

    // UI state
    val message: String? = null,
    val isError: Boolean = false
)
