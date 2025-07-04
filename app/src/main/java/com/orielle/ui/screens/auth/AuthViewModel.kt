package com.orielle.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.orielle.domain.model.Response
import com.orielle.domain.use_case.SignInUseCase
import com.orielle.domain.use_case.SignInWithAppleUseCase
import com.orielle.domain.use_case.SignInWithGoogleUseCase
import com.orielle.domain.use_case.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val signInUseCase: SignInUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signInWithAppleUseCase: SignInWithAppleUseCase,
    private val auth: FirebaseAuth // Inject FirebaseAuth to check current user
) : ViewModel() {

    // --- UI State ---
    private val _isUserAuthenticated = MutableStateFlow<Boolean?>(null)
    val isUserAuthenticated = _isUserAuthenticated.asStateFlow()

    private val _displayName = MutableStateFlow("")
    val displayName = _displayName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _hasAgreedToTerms = MutableStateFlow(false)
    val hasAgreedToTerms = _hasAgreedToTerms.asStateFlow()

    private val _authResponse = MutableStateFlow<Response<Boolean>?>(null)
    val authResponse = _authResponse.asStateFlow()

    init {
        // This function will now correctly reference the 'auth' property of the class.
        checkUserAuthentication()
    }

    private fun checkUserAuthentication() {
        _isUserAuthenticated.value = auth.currentUser != null
    }

    fun onDisplayNameChange(newName: String) {
        _displayName.value = newName
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    fun onTermsAgreementChange(isAgreed: Boolean) {
        _hasAgreedToTerms.value = isAgreed
    }

    fun signUp() {
        viewModelScope.launch {
            signUpUseCase(displayName.value, email.value, password.value).collect { response ->
                _authResponse.value = response
            }
        }
    }

    fun signIn() {
        viewModelScope.launch {
            signInUseCase(email.value, password.value).collect { response ->
                _authResponse.value = response
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            signInWithGoogleUseCase(idToken).collect { response ->
                _authResponse.value = response
            }
        }
    }

    fun signInWithApple(idToken: String) {
        viewModelScope.launch {
            signInWithAppleUseCase(idToken).collect { response ->
                _authResponse.value = response
            }
        }
    }
}
