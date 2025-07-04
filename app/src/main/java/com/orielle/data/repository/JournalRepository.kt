package com.orielle.data.repository

import com.orielle.domain.model.JournalEntry
import com.orielle.domain.model.Response
import kotlinx.coroutines.flow.Flow

/**
 * An interface for the Journal Repository.
 * This defines the contract for all journal-related data operations,
 * acting as the single source of truth for journal entries.
 */
interface JournalRepository {

    /**
     * Retrieves all journal entries for the current user from the local cache.
     * The local cache is kept up-to-date with the remote Firestore database.
     * @return A Flow that emits a list of journal entries.
     */
    fun getJournalEntries(): Flow<List<JournalEntry>>

    /**
     * Saves a new journal entry to the remote database (Firestore) and then
     * caches it locally.
     * @param entry The journal entry to be saved.
     * @return A Flow that emits the response of the save operation.
     */
    fun saveJournalEntry(entry: JournalEntry): Flow<Response<Boolean>>

    /**
     * Deletes a journal entry from both the remote database and the local cache.
     * @param entryId The ID of the journal entry to delete.
     * @return A Flow that emits the response of the delete operation.
     */
    fun deleteJournalEntry(entryId: String): Flow<Response<Boolean>>
}
