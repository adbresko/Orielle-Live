package com.orielle.ui.screens.sanctuary

import app.cash.turbine.test
import com.orielle.domain.model.AppError
import com.orielle.ui.util.UiEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.spy
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class SanctuaryViewModelTest {
    private lateinit var viewModel: SanctuaryViewModel

    @Before
    fun setup() {
        viewModel = spy(SanctuaryViewModel())
    }

    @Test
    fun `emits error event when submitReflection throws`() = runTest {
        // Simulate an error by throwing from delay
        whenever(viewModel.submitReflection()).thenAnswer { throw RuntimeException("AI error") }

        viewModel.eventFlow.test {
            try {
                viewModel.submitReflection()
            } catch (_: Exception) {}
            val event = awaitItem()
            assertEquals("An unknown error occurred.", (event as UiEvent.ShowSnackbar).message)
            cancelAndIgnoreRemainingEvents()
        }
    }
} 