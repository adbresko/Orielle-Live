package com.orielle.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.orielle.data.local.dao.MoodCheckInDao
import com.orielle.data.mapper.toMoodCheckIn
import com.orielle.data.mapper.toMoodCheckInEntity
import com.orielle.domain.manager.SessionManager
import com.orielle.domain.model.AppError
import com.orielle.domain.model.MoodCheckIn
import com.orielle.domain.model.Response
import com.orielle.domain.repository.MoodCheckInRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of MoodCheckInRepository that handles local database operations
 * with Firebase Firestore synchronization.
 */
class MoodCheckInRepositoryImpl @Inject constructor(
    private val moodCheckInDao: MoodCheckInDao,
    private val firestore: FirebaseFirestore,
    private val sessionManager: SessionManager
) : MoodCheckInRepository {

    override suspend fun saveMoodCheckIn(moodCheckIn: MoodCheckIn): Response<Unit> {
        return try {
            val userId = sessionManager.currentUserId.first()
                ?: return Response.Failure(AppError.Auth, Exception("No user session found."))
            val isGuest = sessionManager.isGuest.first()

            val moodToSave = moodCheckIn.copy(
                userId = userId,
                id = if (moodCheckIn.id.isBlank()) UUID.randomUUID().toString() else moodCheckIn.id
            )

            // Always save to local database first (offline-first)
            moodCheckInDao.insertMoodCheckIn(moodToSave.toMoodCheckInEntity())

            // Try to sync to Firebase if not a guest user
            Timber.d("🔍 Mood sync check - isGuest: $isGuest, userId: $userId")
            if (!isGuest) {
                try {
                    Timber.d("🚀 Attempting Firebase sync for mood check-in: ${moodToSave.id}")
                    firestore.collection("users").document(userId)
                        .collection("mood_check_ins").document(moodToSave.id)
                        .set(moodToSave).await()
                    Timber.d("✅ Mood check-in synced to Firebase successfully")
                } catch (firebaseError: Exception) {
                    // Firebase sync failed, but local save succeeded
                    Timber.w(firebaseError, "❌ Failed to sync mood check-in to Firebase, saved locally")
                    FirebaseCrashlytics.getInstance().recordException(firebaseError)
                    // Don't fail the operation - data is saved locally and will sync later
                }
            } else {
                Timber.d("⏭️ Skipping Firebase sync - user is guest")
            }

            Response.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error saving mood check-in")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }

    override fun getMoodCheckInsByUserId(userId: String): Flow<Response<List<MoodCheckIn>>> {
        return moodCheckInDao.getMoodCheckInsByUserId(userId).map { entities ->
            try {
                Response.Success(entities.map { it.toMoodCheckIn() })
            } catch (e: Exception) {
                Response.Failure(AppError.Database, e)
            }
        }
    }

    override suspend fun hasMoodCheckInForDate(userId: String, date: Date): Response<Boolean> {
        return try {
            val hasCheckIn = moodCheckInDao.hasMoodCheckInForDate(userId, date)
            Response.Success(hasCheckIn)
        } catch (e: Exception) {
            Response.Failure(AppError.Database, e)
        }
    }

    override suspend fun getMoodCheckInForDate(userId: String, date: Date): Response<MoodCheckIn?> {
        return try {
            val entity = moodCheckInDao.getMoodCheckInForDate(userId, date)
            Response.Success(entity?.toMoodCheckIn())
        } catch (e: Exception) {
            Response.Failure(AppError.Database, e)
        }
    }

    override fun getRecentMoodCheckIns(userId: String, limit: Int): Flow<Response<List<MoodCheckIn>>> {
        return moodCheckInDao.getRecentMoodCheckIns(userId, limit).map { entities ->
            try {
                Response.Success(entities.map { it.toMoodCheckIn() })
            } catch (e: Exception) {
                Response.Failure(AppError.Database, e)
            }
        }
    }

    override suspend fun deleteMoodCheckIn(moodCheckIn: MoodCheckIn): Response<Unit> {
        return try {
            val userId = sessionManager.currentUserId.first()
                ?: return Response.Failure(AppError.Auth, Exception("No user session found."))
            val isGuest = sessionManager.isGuest.first()

            // Delete from local database
            moodCheckInDao.deleteMoodCheckIn(moodCheckIn.toMoodCheckInEntity())

            // Delete from Firebase if not a guest user
            if (!isGuest) {
                firestore.collection("users").document(userId)
                    .collection("mood_check_ins").document(moodCheckIn.id)
                    .delete().await()
            }

            Response.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting mood check-in")
            FirebaseCrashlytics.getInstance().recordException(e)
            Response.Failure(AppError.Database, e)
        }
    }
}