package com.orielle.data.manager

import android.content.Context
import android.content.SharedPreferences
import com.orielle.data.local.model.QuoteEntity
import com.orielle.data.repository.QuoteRepository
import com.orielle.data.repository.JournalPromptRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages daily content (quotes, prompts, etc.) to ensure users get the same content
 * for the entire day once they've selected a category, even if they navigate back and forth.
 */
@Singleton
class DailyContentManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val quoteRepository: QuoteRepository,
    private val journalPromptRepository: JournalPromptRepository
) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "daily_content", Context.MODE_PRIVATE
    )

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Gets the quote for today for the specified mood.
     * If no quote exists for today, generates a new one and stores it.
     */
    suspend fun getTodaysQuote(mood: String): QuoteEntity? {
        return getTodaysContent<QuoteEntity>(
            contentType = "quote",
            category = mood,
            contentGenerator = { quoteRepository.getRandomQuoteByMood(mood) }
        )
    }

    /**
     * Gets the prompt for today for the specified mood.
     * If no prompt exists for today, generates a new one and stores it.
     */
    suspend fun getTodaysPrompt(mood: String): String? {
        return getTodaysContent<String>(
            contentType = "prompt",
            category = mood,
            contentGenerator = {
                val promptEntity = journalPromptRepository.getRandomPromptByMood(mood)
                promptEntity?.promptText
            }
        )
    }

    /**
     * Generic method to get today's content for any type.
     * This allows the same persistence logic for quotes, prompts, etc.
     */
    private suspend fun <T> getTodaysContent(
        contentType: String,
        category: String,
        contentGenerator: suspend () -> T?
    ): T? {
        val today = dateFormat.format(Date())
        val key = "${contentType}_${category.lowercase()}_$today"

        // Check if we already have content for today
        val storedContentId = prefs.getString(key, null)

        if (storedContentId != null) {
            // Try to get the stored content
            @Suppress("UNCHECKED_CAST")
            return getStoredContentForToday(contentType, category, storedContentId) as T?
        } else {
            // Generate new content for today
            return generateNewContentForToday(contentType, category, today, key, contentGenerator)
        }
    }

    private suspend fun getStoredContentForToday(contentType: String, category: String, contentId: String): Any? {
        return when (contentType) {
            "quote" -> {
                val allQuotes = quoteRepository.getAllQuotesByMood(category)
                if (allQuotes.isNotEmpty()) {
                    // Use the contentId as a seed to consistently pick the same quote
                    val seed = contentId.hashCode()
                    val index = kotlin.math.abs(seed) % allQuotes.size
                    allQuotes[index]
                } else null
            }
            "prompt" -> {
                val allPrompts = journalPromptRepository.getPromptsByMood(category)
                if (allPrompts.isNotEmpty()) {
                    // Use the contentId as a seed to consistently pick the same prompt
                    val seed = contentId.hashCode()
                    val index = kotlin.math.abs(seed) % allPrompts.size
                    allPrompts[index].promptText
                } else null
            }
            else -> null
        }
    }

    private suspend fun <T> generateNewContentForToday(
        contentType: String,
        category: String,
        today: String,
        key: String,
        contentGenerator: suspend () -> T?
    ): T? {
        val content = contentGenerator()
        if (content != null) {
            // Store the content identifier for today
            val contentId = when (content) {
                is QuoteEntity -> content.id
                is String -> content
                else -> content.toString()
            }
            prefs.edit()
                .putString(key, contentId)
                .apply()
        }
        return content
    }

    /**
     * Clears all stored daily content (useful for testing or reset functionality)
     */
    fun clearAllDailyContent() {
        prefs.edit().clear().apply()
    }

    /**
     * Gets the stored content ID for today for debugging purposes
     */
    fun getStoredContentIdForToday(contentType: String, category: String): String? {
        val today = dateFormat.format(Date())
        val key = "${contentType}_${category.lowercase()}_$today"
        return prefs.getString(key, null)
    }
}
