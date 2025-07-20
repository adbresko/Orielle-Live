package com.orielle.data.repository

import com.orielle.data.local.dao.MoodCheckInDao
import com.orielle.data.mapper.toMoodCheckIn
import com.orielle.data.mapper.toMoodCheckInEntity
import com.orielle.domain.model.AppError
import com.orielle.domain.model.MoodCheckIn
import com.orielle.domain.model.Response
import com.orielle.domain.repository.MoodCheckInRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Date
import java.util.UUID
import javax.inject.Inject

/**
 * Implementation of MoodCheckInRepository that handles local database operations.
 */
class MoodCheckInRepositoryImpl @Inject constructor(
    private val moodCheckInDao: MoodCheckInDao
) : MoodCheckInRepository {

    override suspend fun saveMoodCheckIn(moodCheckIn: MoodCheckIn): Response<Unit> {
        return try {
            val entity = if (moodCheckIn.id.isEmpty()) {
                moodCheckIn.copy(id = UUID.randomUUID().toString())
            } else {
                moodCheckIn
            }.toMoodCheckInEntity()

            moodCheckInDao.insertMoodCheckIn(entity)
            Response.Success(Unit)
        } catch (e: Exception) {
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
            moodCheckInDao.deleteMoodCheckIn(moodCheckIn.toMoodCheckInEntity())
            Response.Success(Unit)
        } catch (e: Exception) {
            Response.Failure(AppError.Database, e)
        }
    }
}