package com.orielle.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.orielle.domain.model.EmailAlreadyInUseException
import com.orielle.domain.model.Response
import com.orielle.domain.model.WeakPasswordException
import com.orielle.domain.use_case.SignInUseCase
import com.orielle.domain.use_case.SignInWithAppleUseCase
import com.orielle.domain.use_case.SignInWithGoogleUseCase
import com.orielle.domain.use_case.SignUpUseCase
import com.orielle.util.UiEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class PasswordStrength {
    object None : PasswordStrength()
    object Weak : PasswordStrength()
    object Medium : PasswordStrength()
    object Strong : PasswordStrength()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val signInUseCase: SignInUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val signInWithAppleUseCase: SignInWithAppleUseCase,
    private val auth: FirebaseAuth,
) : ViewModel() {

    // --- Authentication Status State ---
    private val _isUserAuthenticated = MutableStateFlow<Boolean?>(null)
    val isUserAuthenticated = _isUserAuthenticated.asStateFlow()

    // --- NEW: State to track if the user just signed up ---
    private val _isNewUserAfterSignUp = MutableStateFlow(false)
    val isNewUserAfterSignUp = _isNewUserAfterSignUp.asStateFlow()

    // --- Form Input State ---
    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _confirmPassword = MutableStateFlow("")
    val confirmPassword = _confirmPassword.asStateFlow()

    private val _hasAgreedToTerms = MutableStateFlow(false)
    val hasAgreedToTerms = _hasAgreedToTerms.asStateFlow()

    // --- UI State ---
    private val _passwordStrength = MutableStateFlow<PasswordStrength>(PasswordStrength.None)
    val passwordStrength = _passwordStrength.asStateFlow()

    private val _emailError = MutableStateFlow<AuthFieldError>(AuthFieldError.None)
    val emailError = _emailError.asStateFlow()

    private val _passwordError = MutableStateFlow<AuthFieldError>(AuthFieldError.None)
    val passwordError = _passwordError.asStateFlow()

    private val _confirmPasswordError = MutableStateFlow<AuthFieldError>(AuthFieldError.None)
    val confirmPasswordError = _confirmPasswordError.asStateFlow()


    // --- Async Operation State ---
    // This is now more flexible to hold different types of success data
    private val _authResponse = MutableStateFlow<Response<out Any>?>(null)
    val authResponse = _authResponse.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        checkUserAuthentication()
    }

    private fun checkUserAuthentication() {
        _isUserAuthenticated.value = auth.currentUser != null
    }

    // --- Event Handlers ---
    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
        if (_emailError.value !is AuthFieldError.None) _emailError.value = AuthFieldError.None
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
        if (_passwordError.value !is AuthFieldError.None) _passwordError.value = AuthFieldError.None

        _passwordStrength.value = when {
            newPassword.isBlank() -> PasswordStrength.None
            newPassword.length < 8 -> PasswordStrength.Weak
            newPassword.any { it.isDigit() } && newPassword.any { it.isLetter() } && newPassword.any { !it.isLetterOrDigit() } -> PasswordStrength.Strong
            newPassword.any { it.isDigit() } && newPassword.any { it.isLetter() } -> PasswordStrength.Medium
            else -> PasswordStrength.Weak
        }
    }

    fun onConfirmPasswordChange(newPassword: String) {
        _confirmPassword.value = newPassword
        if (_confirmPasswordError.value !is AuthFieldError.None) _confirmPasswordError.value = AuthFieldError.None
    }

    fun onTermsAgreementChange(isAgreed: Boolean) {
        _hasAgreedToTerms.value = isAgreed
    }

    // --- Core Logic ---
    fun signUp() {
        if (password.value != confirmPassword.value) {
            _confirmPasswordError.value = AuthFieldError.SimpleError("Passwords do not match.")
            return
        }
        if (!_hasAgreedToTerms.value) {
            viewModelScope.launch {
                _eventFlow.emit(UiEvent.ShowSnackbar("You must agree to the Terms & Conditions."))
            }
            return
        }

        viewModelScope.launch {
            _authResponse.value = Response.Loading
            // The use case now returns a Response<FirebaseUser>
            signUpUseCase(email.value, password.value).collect { response ->
                if (response is Response.Success) {
                    // --- THIS IS THE KEY NEW LOGIC ---
                    // On successful sign-up, check the user's metadata.
                    val user = response.data
                    val metadata = user.metadata
                    if (metadata != null) {
                        val creationTime = metadata.creationTimestamp
                        val lastSignInTime = metadata.lastSignInTimestamp
                        // If the account was created within the last 15 seconds,
                        // we can be very sure this is their first session.
                        if ((lastSignInTime - creationTime) < 15000) {
                            _isNewUserAfterSignUp.value = true
                        }
                    }
                } else if (response is Response.Failure) {
                    when (val exception = response.exception) {
                        is EmailAlreadyInUseException -> {
                            _emailError.value = AuthFieldError.EmailAlreadyInUse(exception.message ?: "This email is already registered.")
                        }
                        is WeakPasswordException -> {
                            _passwordError.value = AuthFieldError.SimpleError(exception.message ?: "Password is too weak.")
                        }
                        else -> {
                            _eventFlow.emit(UiEvent.ShowSnackbar(exception.message ?: "An unknown error occurred."))
                        }
                    }
                }
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
}