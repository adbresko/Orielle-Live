package com.orielle.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.orielle.data.local.dao.JournalDao
import com.orielle.data.mapper.toDomain
import com.orielle.data.mapper.toEntity
import com.orielle.domain.model.JournalEntry
import com.orielle.domain.model.Response
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class JournalRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val journalDao: JournalDao
) : JournalRepository {

    private val userId: String
        get() = auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")

    override fun getJournalEntries(): Flow<List<JournalEntry>> {
        // This flow reads directly from the local Room database.
        // We will set up a separate listener to keep Room in sync with Firestore.
        return journalDao.getJournalEntries(userId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun saveJournalEntry(entry: JournalEntry): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            // Ensure the entry has a user ID and generate a new ID if it's a new entry.
            val entryToSave = entry.copy(
                userId = userId,
                id = if (entry.id.isBlank()) UUID.randomUUID().toString() else entry.id
            )

            // Save to Firestore first (single source of truth)
            firestore.collection("users").document(userId)
                .collection("journal_entries").document(entryToSave.id)
                .set(entryToSave).await()

            // Then save to the local Room cache
            journalDao.upsertJournalEntry(entryToSave.toEntity())

            emit(Response.Success(true))
        } catch (e: Exception) {
            emit(Response.Failure(e))
        }
    }

    override fun deleteJournalEntry(entryId: String): Flow<Response<Boolean>> = flow {
        emit(Response.Loading)
        try {
            // Delete from Firestore first
            firestore.collection("users").document(userId)
                .collection("journal_entries").document(entryId)
                .delete().await()

            // Then delete from the local Room cache
            journalDao.deleteJournalEntry(entryId)

            emit(Response.Success(true))
        } catch (e: Exception) {
            emit(Response.Failure(e))
        }
    }
}
