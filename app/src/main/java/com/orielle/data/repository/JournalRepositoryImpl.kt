package com.orielle.data.repository

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
import com.orielle.domain.model.AppError
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.orielle.domain.repository.JournalRepository
import timber.log.Timber

class JournalRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val journalDao: JournalDao,
    private val sessionManager: SessionManager // New dependency
) : JournalRepository {

    override fun getJournalEntries(): Flow<List<JournalEntry>> = flow {
        try {
            val userId = sessionManager.currentUserId.first()
            if (userId != null) {
                journalDao.getJournalEntries(userId).map { entities ->
                    entities.map { it.toDomain() }
                }.collect { emit(it) }
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching journal entries")
            FirebaseCrashlytics.getInstance().recordException(e)
            throw e // Let the ViewModel handle via .catch
        }
    }

    override suspend fun saveJournalEntry(entry: JournalEntry): Response<Boolean> {
        return try {
            val userId = sessionManager.currentUserId.first()
                ?: return Response.Failure(AppError.Auth, Exception("No user session found."))
            val isGuest = sessionManager.isGuest.first()

            val entryToSave = entry.copy(
                userId = userId,
                id = if (entry.id.isBlank()) UUID.randomUUID().toString() else entry.id
            )

            journalDao.upsertJournalEntry(entryToSave.toEntity())

            if (!isGuest) {
                firestore.collection("users").document(userId)
                    .collection("journal_entries").document(entryToSave.id)
                    .set(entryToSave).await()
            }
            Response.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error saving journal entry")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    override suspend fun deleteJournalEntry(entryId: String): Response<Boolean> {
        return try {
            val userId = sessionManager.currentUserId.first()
                ?: return Response.Failure(AppError.Auth, Exception("No user session found."))
            val isGuest = sessionManager.isGuest.first()

            journalDao.deleteJournalEntry(entryId)

            if (!isGuest) {
                firestore.collection("users").document(userId)
                    .collection("journal_entries").document(entryId)
                    .delete().await()
            }
            Response.Success(true)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting journal entry")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }
}