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
    private val auth: FirebaseAuth,
) : ViewModel() {

    // --- UI State for Authentication Status ---
    private val _isUserAuthenticated = MutableStateFlow<Boolean?>(null)
    val isUserAuthenticated = _isUserAuthenticated.asStateFlow()

    // --- UI State for Forms ---
    private val _displayName = MutableStateFlow("")
    val displayName = _displayName.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    // --- ADDED: State for new fields ---
    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword = _confirmPassword.asStateFlow()

    private val _hasAgreedToTerms = MutableStateFlow(false)
    val hasAgreedToTerms = _hasAgreedToTerms.asStateFlow()

    // --- UI State for Async Responses ---
    private val _authResponse = MutableStateFlow<Response<Boolean>?>(null)
    val authResponse = _authResponse.asStateFlow()

    init {
        checkUserAuthentication()
    }

    private fun checkUserAuthentication() {
        _isUserAuthenticated.value = auth.currentUser != null
    }

    // --- Event Handlers for Form Inputs ---
    fun onDisplayNameChange(newName: String) {
        _displayName.value = newName
    }

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    // --- ADDED: Event Handlers for new fields ---
    fun onConfirmPasswordChange(newPassword: String) {
        _confirmPassword.value = newPassword
    }

    fun onTermsAgreementChange(isAgreed: Boolean) {
        _hasAgreedToTerms.value = isAgreed
    }


    // --- Authentication Logic ---
    fun signUp() {
        viewModelScope.launch {
            _authResponse.value = Response.Loading
            signUpUseCase(displayName.value, email.value, password.value).collect { response ->
                _authResponse.value = response
            }
        }
    }

    fun signIn() {
        viewModelScope.launch {
            _authResponse.value = Response.Loading
            signInUseCase(email.value, password.value).collect { response ->
                _authResponse.value = response
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authResponse.value = Response.Loading
            signInWithGoogleUseCase(idToken).collect { response ->
                _authResponse.value = response
            }
        }
    }

    fun signInWithApple(idToken: String) {
        viewModelScope.launch {
            _authResponse.value = Response.Loading
            signInWithAppleUseCase(idToken).collect { response ->
                _authResponse.value = response
            }
        }
    }
}