package com.orielle.domain.use_case

import com.orielle.domain.model.JournalEntry
import com.orielle.domain.repository.JournalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Use case for getting a specific journal entry by its ID.
 */
class GetJournalEntryByIdUseCase @Inject constructor(
    private val getJournalEntriesUseCase: GetJournalEntriesUseCase
) {
    /**
     * Gets a journal entry by its ID.
     * @param entryId The ID of the entry to retrieve.
     * @return A Flow that emits the journal entry if found, or null if not found.
     */
    operator fun invoke(entryId: String): Flow<JournalEntry?> {
        return getJournalEntriesUseCase().map { entries ->
            entries.find { it.id == entryId }
        }
    }
}
