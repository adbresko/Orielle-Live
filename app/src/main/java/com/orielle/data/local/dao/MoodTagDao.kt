package com.orielle.data.local.dao

import androidx.room.*
import com.orielle.data.local.model.MoodTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodTagDao {

    @Query("SELECT * FROM mood_tags WHERE userId IS NULL OR userId = :userId ORDER BY name ASC")
    fun getMoodTagsForUser(userId: String): Flow<List<MoodTagEntity>>

    @Query("SELECT * FROM mood_tags WHERE userId IS NULL ORDER BY name ASC")
    fun getDefaultMoodTags(): Flow<List<MoodTagEntity>>

    @Query("SELECT * FROM mood_tags WHERE userId = :userId ORDER BY name ASC")
    fun getCustomMoodTags(userId: String): Flow<List<MoodTagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodTag(moodTag: MoodTagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodTags(moodTags: List<MoodTagEntity>)

    @Update
    suspend fun updateMoodTag(moodTag: MoodTagEntity)

    @Delete
    suspend fun deleteMoodTag(moodTag: MoodTagEntity)

    @Query("DELETE FROM mood_tags WHERE userId = :userId AND isCustom = 1")
    suspend fun deleteCustomMoodTags(userId: String)
}