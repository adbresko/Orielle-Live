package com.orielle.ui.screens.auth

import app.cash.turbine.test
import com.orielle.domain.model.AppError
import com.orielle.domain.model.Response
import com.orielle.domain.use_case.SignInUseCase
import com.orielle.domain.use_case.SignUpUseCase
import com.orielle.domain.use_case.SignInWithGoogleUseCase
import com.orielle.domain.use_case.SignInWithAppleUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import com.orielle.ui.util.UiEvent

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {
    private lateinit var signInUseCase: SignInUseCase
    private lateinit var signUpUseCase: SignUpUseCase
    private lateinit var signInWithGoogleUseCase: SignInWithGoogleUseCase
    private lateinit var signInWithAppleUseCase: SignInWithAppleUseCase
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setup() {
        signInUseCase = mock()
        signUpUseCase = mock()
        signInWithGoogleUseCase = mock()
        signInWithAppleUseCase = mock()
        viewModel = AuthViewModel(signUpUseCase, signInUseCase, signInWithGoogleUseCase, signInWithAppleUseCase, mock())
    }

    @Test
    fun `emits error event when signInUseCase fails`() = runTest {
        whenever(signInUseCase.invoke(any(), any())).thenReturn(flow {
            emit(Response.Failure(AppError.Auth, Exception("Auth failed")))
        })

        viewModel.eventFlow.test {
            viewModel.signIn()
            val event = awaitItem()
            assertEquals("Authentication failed.", (event as UiEvent.ShowSnackbar).message)
            cancelAndIgnoreRemainingEvents()
        }
    }
} 