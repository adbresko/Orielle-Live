package com.orielle.domain.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.orielle.data.local.dao.JournalDao
import com.orielle.data.mapper.toDomain
import com.orielle.data.mapper.toEntity
import com.orielle.domain.manager.SessionManager
import com.orielle.domain.model.JournalEntry
import com.orielle.domain.model.Response
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject

class JournalRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val journalDao: JournalDao,
    private val sessionManager: SessionManager // New dependency
) : JournalRepository {

    // getJournalEntries now correctly reads from the DAO based on the current session ID
    override fun getJournalEntries(): Flow<List<JournalEntry>> = flow {
        val userId = sessionManager.currentUserId.first()
        if (userId != null) {
            journalDao.getJournalEntries(userId).map { entities ->
                entities.map { it.toDomain() }
            }.collect { emit(it) }
        } else {
            emit(emptyList()) // Emit empty list if no session
        }
    }

    // saveJournalEntry now checks if the user is a guest
    override suspend fun saveJournalEntry(entry: JournalEntry): Response<Boolean> {
        return try {
            val userId = sessionManager.currentUserId.first()
                ?: return Response.Failure(Exception("No user session found."))
            val isGuest = sessionManager.isGuest.first()

            val entryToSave = entry.copy(
                userId = userId,
                id = if (entry.id.isBlank()) UUID.randomUUID().toString() else entry.id
            )

            // Always save to the local Room cache
            journalDao.upsertJournalEntry(entryToSave.toEntity())

            // Only save to Firestore if the user is NOT a guest
            if (!isGuest) {
                firestore.collection("users").document(userId)
                    .collection("journal_entries").document(entryToSave.id)
                    .set(entryToSave).await()
            }
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }

    // deleteJournalEntry now checks if the user is a guest
    override suspend fun deleteJournalEntry(entryId: String): Response<Boolean> {
        return try {
            val userId = sessionManager.currentUserId.first()
                ?: return Response.Failure(Exception("No user session found."))
            val isGuest = sessionManager.isGuest.first()

            // Always delete from the local Room cache
            journalDao.deleteJournalEntry(entryId)

            // Only delete from Firestore if the user is NOT a guest
            if (!isGuest) {
                firestore.collection("users").document(userId)
                    .collection("journal_entries").document(entryId)
                    .delete().await()
            }
            Response.Success(true)
        } catch (e: Exception) {
            Response.Failure(e)
        }
    }
}