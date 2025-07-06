package com.orielle.domain.use_case

import com.orielle.data.repository.JournalRepository
import com.orielle.domain.model.JournalEntry
import javax.inject.Inject

class SaveJournalEntryUseCase @Inject constructor(
    private val repository: JournalRepository
) {
    // Add the "suspend" modifier here
    suspend operator fun invoke(entry: JournalEntry) = repository.saveJournalEntry(entry)
}