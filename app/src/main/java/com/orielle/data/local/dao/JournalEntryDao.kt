package com.orielle.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.orielle.data.local.model.JournalEntryEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the journal_entries table.
 */
@Dao
interface JournalDao {

    /**
     * Inserts a journal entry if it's new, or updates it if it already exists.
     * @param entry The journal entry to be saved.
     */
    @Upsert
    suspend fun upsertJournalEntry(entry: JournalEntryEntity)

    /**
     * Retrieves all journal entries for a specific user, ordered by the most recent first.
     * Returns a Flow, so the UI can reactively update whenever the data changes.
     * @param userId The ID of the user whose entries are to be fetched.
     * @return A Flow emitting a list of journal entries.
     */
    @Query("SELECT * FROM journal_entries WHERE userId = :userId ORDER BY timestamp DESC")
    fun getJournalEntries(userId: String): Flow<List<JournalEntryEntity>>

    /**
     * Deletes a specific journal entry by its ID.
     * @param entryId The ID of the journal entry to delete.
     */
    @Query("DELETE FROM journal_entries WHERE id = :entryId")
    suspend fun deleteJournalEntry(entryId: String)
}
