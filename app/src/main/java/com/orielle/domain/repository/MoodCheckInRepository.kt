package com.orielle.domain.repository

import com.orielle.domain.model.MoodCheckIn
import com.orielle.domain.model.Response
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository interface for mood check-in operations.
 * Defines the contract for mood check-in data operations.
 */
interface MoodCheckInRepository {

    /**
     * Save a new mood check-in.
     */
    suspend fun saveMoodCheckIn(moodCheckIn: MoodCheckIn): Response<Unit>

    /**
     * Get all mood check-ins for a specific user.
     */
    fun getMoodCheckInsByUserId(userId: String): Flow<Response<List<MoodCheckIn>>>

    /**
     * Check if a user has already submitted a mood check-in for a specific date.
     */
    suspend fun hasMoodCheckInForDate(userId: String, date: Date): Response<Boolean>

    /**
     * Get the mood check-in for a specific date.
     */
    suspend fun getMoodCheckInForDate(userId: String, date: Date): Response<MoodCheckIn?>

    /**
     * Get recent mood check-ins for a user.
     */
    fun getRecentMoodCheckIns(userId: String, limit: Int = 7): Flow<Response<List<MoodCheckIn>>>

    /**
     * Delete a mood check-in.
     */
    suspend fun deleteMoodCheckIn(moodCheckIn: MoodCheckIn): Response<Unit>
}