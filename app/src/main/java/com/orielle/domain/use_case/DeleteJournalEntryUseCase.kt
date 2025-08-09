package com.orielle.domain.use_case

import com.orielle.domain.repository.JournalRepository
import javax.inject.Inject

/**
 * Use case for deleting a journal entry.
 */
class DeleteJournalEntryUseCase @Inject constructor(
    private val repository: JournalRepository
) {
    /**
     * Deletes a journal entry by its ID.
     * @param entryId The ID of the entry to delete.
     */
    suspend operator fun invoke(entryId: String) = repository.deleteJournalEntry(entryId)
}
