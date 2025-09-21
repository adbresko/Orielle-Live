package com.orielle.data.local.dao

import androidx.room.*
import com.orielle.data.local.model.MoodCheckInEntity
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface MoodCheckInDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodCheckIn(moodCheckIn: MoodCheckInEntity)

    @Query("SELECT * FROM mood_check_ins WHERE userId = :userId ORDER BY timestamp DESC")
    fun getMoodCheckInsByUserId(userId: String): Flow<List<MoodCheckInEntity>>

    // Use 'localtime' modifier to ensure calendar day detection works in user's local timezone
    // This ensures daily check-ins reset at midnight local time, not UTC
    @Query("SELECT * FROM mood_check_ins WHERE userId = :userId AND DATE(timestamp/1000, 'unixepoch', 'localtime') = DATE(:date/1000, 'unixepoch', 'localtime') LIMIT 1")
    suspend fun getMoodCheckInForDate(userId: String, date: Date): MoodCheckInEntity?

    // Use 'localtime' modifier to ensure calendar day detection works in user's local timezone
    // This ensures daily check-ins reset at midnight local time, not UTC
    @Query("SELECT EXISTS(SELECT 1 FROM mood_check_ins WHERE userId = :userId AND DATE(timestamp/1000, 'unixepoch', 'localtime') = DATE(:date/1000, 'unixepoch', 'localtime'))")
    suspend fun hasMoodCheckInForDate(userId: String, date: Date): Boolean

    @Query("SELECT * FROM mood_check_ins WHERE userId = :userId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentMoodCheckIns(userId: String, limit: Int = 7): Flow<List<MoodCheckInEntity>>

    @Delete
    suspend fun deleteMoodCheckIn(moodCheckIn: MoodCheckInEntity)

    @Query("DELETE FROM mood_check_ins WHERE userId = :userId")
    suspend fun deleteAllMoodCheckInsForUser(userId: String)
}