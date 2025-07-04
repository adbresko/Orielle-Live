package com.orielle.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orielle.domain.model.Response
import com.orielle.domain.use_case.SignInUseCase
import com.orielle.domain.use_case.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val signInUseCase: SignInUseCase
) : ViewModel() {

    // UI State for the input fields
    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    // UI State for the authentication response (Loading, Success, Failure)
    private val _authResponse = MutableStateFlow<Response<Boolean>?>(null)
    val authResponse = _authResponse.asStateFlow()

    fun onEmailChange(newEmail: String) {
        _email.value = newEmail
    }

    fun onPasswordChange(newPassword: String) {
        _password.value = newPassword
    }

    /**
     * Triggers the sign-up process by launching a coroutine
     * and calling the SignUpUseCase.
     */
    fun signUp() {
        viewModelScope.launch {
            signUpUseCase(email.value, password.value).collect { response ->
                _authResponse.value = response
            }
        }
    }

    /**
     * Triggers the sign-in process by launching a coroutine
     * and calling the SignInUseCase.
     */
    fun signIn() {
        viewModelScope.launch {
            signInUseCase(email.value, password.value).collect { response ->
                _authResponse.value = response
            }
        }
    }
}
