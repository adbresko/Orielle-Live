package com.orielle.domain.repository

import com.orielle.domain.model.JournalEntry
import com.orielle.domain.model.Response
import kotlinx.coroutines.flow.Flow

interface JournalRepository {
    // This now returns a simple Flow<List<JournalEntry>> as it comes from Room
    fun getJournalEntries(): Flow<List<JournalEntry>>

    // These are now suspend functions for single operations
    suspend fun saveJournalEntry(entry: JournalEntry): Response<Boolean>

    suspend fun deleteJournalEntry(entryId: String): Response<Boolean>
}