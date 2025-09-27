package com.orielle.data.local.dao

import androidx.room.*
import com.orielle.data.local.model.JournalPromptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JournalPromptDao {

    @Query("SELECT * FROM journal_prompts WHERE LOWER(moodCategory) = LOWER(:moodCategory)")
    suspend fun getPromptsByMood(moodCategory: String): List<JournalPromptEntity>

    @Query("SELECT * FROM journal_prompts WHERE LOWER(moodCategory) = LOWER(:moodCategory) ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomPromptByMood(moodCategory: String): JournalPromptEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompts(prompts: List<JournalPromptEntity>)

    @Query("SELECT COUNT(*) FROM journal_prompts")
    suspend fun getPromptCount(): Int

    @Query("DELETE FROM journal_prompts")
    suspend fun deleteAllPrompts()

    @Query("SELECT * FROM journal_prompts")
    suspend fun getAllPrompts(): List<JournalPromptEntity>
}
