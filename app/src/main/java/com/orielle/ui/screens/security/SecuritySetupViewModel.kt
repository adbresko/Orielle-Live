package com.orielle.ui.screens.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.manager.BiometricAuthManager
import com.orielle.domain.manager.SessionManager // We will use this to save the preference
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecuritySetupUiState(
    val isBiometricAuthAvailable: Boolean = false,
    val isBiometricAuthEnabled: Boolean = false
)

@HiltViewModel
class SecuritySetupViewModel @Inject constructor(
    private val biometricAuthManager: BiometricAuthManager,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SecuritySetupUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // When the ViewModel is created, immediately check if biometrics are available.
        checkBiometricAvailability()
    }

    private fun checkBiometricAvailability() {
        _uiState.update { it.copy(isBiometricAuthAvailable = biometricAuthManager.isBiometricAuthAvailable()) }
    }

    // --- Event Handlers ---
    fun onBiometricAuthToggled(isEnabled: Boolean) {
        // For now, we just update the UI state.
        // A full implementation would involve showing the biometric prompt for confirmation.
        _uiState.update { it.copy(isBiometricAuthEnabled = isEnabled) }
    }

    // This function will be called when the user presses "Set Up Now" or "Skip".
    fun onCompleteSetup() {
        viewModelScope.launch {
            // Here, we would save the user's preference to our SessionManager.
            // sessionManager.setBiometricAuthEnabled(uiState.value.isBiometricAuthEnabled)
        }
    }
}