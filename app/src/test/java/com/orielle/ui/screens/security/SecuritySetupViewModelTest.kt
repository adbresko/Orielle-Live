package com.orielle.ui.screens.security

import app.cash.turbine.test
import com.orielle.domain.manager.BiometricAuthManager
import com.orielle.domain.manager.SessionManager
import com.orielle.ui.util.UiEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SecuritySetupViewModelTest {
    private lateinit var biometricAuthManager: BiometricAuthManager
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: SecuritySetupViewModel

    @Before
    fun setup() {
        biometricAuthManager = mock()
        sessionManager = mock()
        viewModel = SecuritySetupViewModel(biometricAuthManager, sessionManager)
    }

    @Test
    fun `emits error event when checkBiometricAvailability throws`() = runTest {
        whenever(biometricAuthManager.isBiometricAuthAvailable()).thenThrow(RuntimeException("Biometric error"))

        viewModel.eventFlow.test {
            viewModel.retryCheckBiometric()
            val event = awaitItem()
            assertEquals("An unknown error occurred.", (event as UiEvent.ShowSnackbar).message)
            cancelAndIgnoreRemainingEvents()
        }
    }
} 