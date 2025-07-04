package com.orielle.domain.use_case

import com.orielle.data.repository.JournalRepository
import com.orielle.domain.model.JournalEntry
import javax.inject.Inject

/**
 * A use case that encapsulates the business logic for saving a new journal entry.
 * Its single responsibility is to call the repository to save the data.
 *
 * @param repository The journal repository, provided by Hilt.
 */
class SaveJournalEntryUseCase @Inject constructor(
    private val repository: JournalRepository
) {
    /**
     * Executes the operation to save a journal entry.
     * The `operator fun invoke` allows us to call this class as if it were a function.
     * @param entry The JournalEntry object to be saved.
     * @return A Flow that emits the response of the save operation.
     */
    operator fun invoke(entry: JournalEntry) = repository.saveJournalEntry(entry)
}
