package com.orielle.ui.screens.home

import app.cash.turbine.test
import com.orielle.domain.model.AppError
import com.orielle.domain.repository.JournalRepository
import com.orielle.domain.use_case.GetJournalEntriesUseCase
import com.orielle.ui.screens.home.HomeViewModel
import com.orielle.ui.util.UiEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    private lateinit var repository: JournalRepository
    private lateinit var useCase: GetJournalEntriesUseCase
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setup() {
        repository = mock()
        useCase = GetJournalEntriesUseCase(repository)
        viewModel = HomeViewModel(useCase, mock())
    }

    @Test
    fun `emits error event when repository throws`() = runTest {
        whenever(repository.getJournalEntries()).thenReturn(flow { throw RuntimeException("DB error") })

        viewModel.eventFlow.test {
            viewModel.retryFetch()
            val event = awaitItem()
            assertEquals("A database error occurred.", (event as UiEvent.ShowSnackbar).message)
            cancelAndIgnoreRemainingEvents()
        }
    }
} 