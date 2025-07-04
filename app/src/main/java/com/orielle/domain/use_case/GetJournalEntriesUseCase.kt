package com.orielle.domain.use_case

import com.orielle.data.repository.JournalRepository
import javax.inject.Inject

/**
 * A use case that encapsulates the business logic for retrieving all of a user's journal entries.
 * Its single responsibility is to call the repository to get the data.
 *
 * @param repository The journal repository, provided by Hilt.
 */
class GetJournalEntriesUseCase @Inject constructor(
    private val repository: JournalRepository
) {
    /**
     * Executes the operation to get journal entries.
     * The `operator fun invoke` allows us to call this class as if it were a function.
     * @return A Flow that emits a list of journal entries.
     */
    operator fun invoke() = repository.getJournalEntries()
}
