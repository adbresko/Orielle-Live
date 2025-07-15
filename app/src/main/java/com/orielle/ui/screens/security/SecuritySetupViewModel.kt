package com.orielle.ui.screens.security

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.manager.BiometricAuthManager
import com.orielle.domain.manager.SessionManager // We will use this to save the preference
import com.orielle.domain.model.AppError
import com.orielle.ui.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import timber.log.Timber

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

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable, "Unhandled coroutine exception in SecuritySetupViewModel")
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar(AppError.Unknown.toUserMessage()))
        }
    }

    init {
        checkBiometricAvailability()
    }

    private fun checkBiometricAvailability() {
        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                _uiState.update { it.copy(isBiometricAuthAvailable = biometricAuthManager.isBiometricAuthAvailable()) }
            } catch (e: Exception) {
                Timber.e(e, "Error checking biometric availability")
                _eventFlow.emit(UiEvent.ShowSnackbar(AppError.Unknown.toUserMessage()))
            }
        }
    }

    fun retryCheckBiometric() {
        checkBiometricAvailability()
    }

    // --- Event Handlers ---
    fun onBiometricAuthToggled(isEnabled: Boolean) {
        _uiState.update { it.copy(isBiometricAuthEnabled = isEnabled) }
    }

    fun onCompleteSetup() {
        viewModelScope.launch(coroutineExceptionHandler) {
            try {
                // Here, we would save the user's preference to our SessionManager.
                // sessionManager.setBiometricAuthEnabled(uiState.value.isBiometricAuthEnabled)
            } catch (e: Exception) {
                Timber.e(e, "Error completing security setup")
                _eventFlow.emit(UiEvent.ShowSnackbar(AppError.Unknown.toUserMessage()))
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