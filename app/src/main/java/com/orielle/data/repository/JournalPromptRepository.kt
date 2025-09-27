package com.orielle.data.repository

import android.content.Context
import com.orielle.data.local.dao.JournalPromptDao
import com.orielle.data.local.model.JournalPromptEntity
import com.orielle.util.JournalPromptCsvParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JournalPromptRepository @Inject constructor(
    private val journalPromptDao: JournalPromptDao,
    private val context: Context
) {

    suspend fun initializePrompts() {
        val existingCount = journalPromptDao.getPromptCount()
        android.util.Log.d("JournalPromptRepository", "🔍 Current prompt count: $existingCount")
        if (existingCount == 0) {
            android.util.Log.d("JournalPromptRepository", "📥 Initializing journal prompts from CSV")
            val prompts = JournalPromptCsvParser.parseJournalPromptsCsv(context)
            android.util.Log.d("JournalPromptRepository", "📊 Parsed ${prompts.size} prompts from CSV")
            if (prompts.isNotEmpty()) {
                android.util.Log.d("JournalPromptRepository", "📝 Sample prompts: ${prompts.take(3).map { "${it.moodCategory}: ${it.promptText}" }}")
            }
            journalPromptDao.insertPrompts(prompts)
            android.util.Log.d("JournalPromptRepository", "✅ Inserted ${prompts.size} prompts into database")
        } else {
            android.util.Log.d("JournalPromptRepository", "ℹ️ Journal prompts already initialized: $existingCount prompts")
        }
    }

    suspend fun getRandomPromptByMood(moodCategory: String): JournalPromptEntity? {
        android.util.Log.d("JournalPromptRepository", "🔍 Getting random prompt for mood: '$moodCategory'")

        // Debug: Check what moods exist in the database
        val allPrompts = journalPromptDao.getAllPrompts()
        val uniqueMoods = allPrompts.map { it.moodCategory }.distinct()
        android.util.Log.d("JournalPromptRepository", "📊 Available moods in DB: $uniqueMoods")

        val prompt = journalPromptDao.getRandomPromptByMood(moodCategory)
        android.util.Log.d("JournalPromptRepository", "📝 Found prompt: ${prompt?.promptText ?: "null"}")
        return prompt
    }

    suspend fun getPromptsByMood(moodCategory: String): List<JournalPromptEntity> {
        return journalPromptDao.getPromptsByMood(moodCategory)
    }

    suspend fun reloadPrompts() {
        android.util.Log.d("JournalPromptRepository", "🔄 Force reloading prompts from CSV")
        journalPromptDao.deleteAllPrompts()
        val prompts = JournalPromptCsvParser.parseJournalPromptsCsv(context)
        android.util.Log.d("JournalPromptRepository", "📊 Parsed ${prompts.size} prompts from CSV")
        journalPromptDao.insertPrompts(prompts)
        android.util.Log.d("JournalPromptRepository", "✅ Reloaded ${prompts.size} prompts into database")
    }
}
