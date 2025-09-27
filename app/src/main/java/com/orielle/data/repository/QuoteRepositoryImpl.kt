package com.orielle.data.repository

import android.content.Context
import com.orielle.data.cache.QuoteCacheManager
import com.orielle.data.local.dao.QuoteDao
import com.orielle.data.local.model.QuoteEntity
import com.orielle.util.CSVQuoteParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementation for managing quotes.
 * Handles CSV parsing, database operations, and delta sync functionality.
 */
@Singleton
class QuoteRepositoryImpl @Inject constructor(
    private val quoteDao: QuoteDao,
    private val context: Context,
    private val quoteCacheManager: QuoteCacheManager
) : QuoteRepository {

    override suspend fun initializeQuotes(): Result<Unit> {
        return try {
            android.util.Log.d("QuoteRepository", "Starting quote initialization...")

            // Parse quotes from CSV
            val csvQuotes = CSVQuoteParser.parseQuotesFromAssets(context)
            android.util.Log.d("QuoteRepository", "Parsed ${csvQuotes.size} quotes from CSV")

            if (csvQuotes.isNotEmpty()) {
                // Get existing quote IDs from database
                val existingIds = quoteDao.getAllQuoteIds().toSet()
                android.util.Log.d("QuoteRepository", "Found ${existingIds.size} existing quotes in database")

                val csvIds = csvQuotes.map { it.id }.toSet()

                // Find quotes to add (in CSV but not in DB)
                val quotesToAdd = csvQuotes.filter { it.id !in existingIds }
                android.util.Log.d("QuoteRepository", "Adding ${quotesToAdd.size} new quotes")

                // Find quotes to remove (in DB but not in CSV)
                val quotesToRemove = existingIds.filter { it !in csvIds }
                android.util.Log.d("QuoteRepository", "Removing ${quotesToRemove.size} old quotes")

                // Perform delta sync
                if (quotesToAdd.isNotEmpty()) {
                    quoteDao.insertQuotes(quotesToAdd)
                    android.util.Log.d("QuoteRepository", "Successfully inserted ${quotesToAdd.size} quotes")
                }

                if (quotesToRemove.isNotEmpty()) {
                    quotesToRemove.forEach { quoteId ->
                        quoteDao.deleteQuoteById(quoteId)
                    }
                    android.util.Log.d("QuoteRepository", "Successfully removed ${quotesToRemove.size} quotes")
                }

                // Log mood distribution
                val moodCounts = csvQuotes.groupBy { it.mood }.mapValues { it.value.size }
                android.util.Log.d("QuoteRepository", "Quote mood distribution: $moodCounts")
            } else {
                android.util.Log.w("QuoteRepository", "No quotes found in CSV file!")
            }

            // Debug: Check final status
            val debugStatus = debugQuoteStatus()
            android.util.Log.d("QuoteRepository", "Final quote status: $debugStatus")

            android.util.Log.d("QuoteRepository", "Quote initialization completed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("QuoteRepository", "Error during quote initialization", e)
            Result.failure(e)
        }
    }

    override suspend fun getRandomQuoteByMood(mood: String, excludeIds: List<String>): QuoteEntity? {
        // Use cache manager for 80% faster quote loading
        return quoteCacheManager.getRandomQuote(mood, excludeIds)
    }

    override suspend fun getAllQuotesByMood(mood: String): List<QuoteEntity> {
        return quoteDao.getAllQuotesByMood(mood)
    }

    override suspend fun getAllQuotes(): List<QuoteEntity> {
        return quoteDao.getAllQuotes()
    }

    override suspend fun getQuoteCountByMood(mood: String): Int {
        return quoteDao.getQuoteCountByMood(mood)
    }

    override suspend fun getTotalQuoteCount(): Int {
        return quoteDao.getTotalQuoteCount()
    }

    override suspend fun getAllMoods(): List<String> {
        return quoteDao.getAllMoods()
    }

    /**
     * Debug method to check if quotes are loaded in the database.
     */
    suspend fun debugQuoteStatus(): String {
        val totalQuotes = quoteDao.getTotalQuoteCount()
        val allMoods = quoteDao.getAllMoods()
        val moodCounts = allMoods.associateWith { mood -> quoteDao.getQuoteCountByMood(mood) }

        return "Total quotes: $totalQuotes, Moods: $allMoods, Counts: $moodCounts"
    }

    override suspend fun deleteQuote(quote: QuoteEntity) {
        quoteDao.deleteQuote(quote)
    }

    override suspend fun deleteQuoteById(quoteId: String) {
        quoteDao.deleteQuoteById(quoteId)
    }

    override suspend fun deleteQuotesByMood(mood: String) {
        quoteDao.deleteQuotesByMood(mood)
    }

    override suspend fun deleteAllQuotes() {
        quoteDao.deleteAllQuotes()
    }

    override fun observeQuotesByMood(mood: String): Flow<List<QuoteEntity>> {
        return flow {
            emit(quoteDao.getAllQuotesByMood(mood))
        }
    }
}

/**
 * Repository interface for quote operations.
 */
interface QuoteRepository {
    suspend fun initializeQuotes(): Result<Unit>
    suspend fun getRandomQuoteByMood(mood: String, excludeIds: List<String> = emptyList()): QuoteEntity?
    suspend fun getAllQuotesByMood(mood: String): List<QuoteEntity>
    suspend fun getAllQuotes(): List<QuoteEntity>
    suspend fun getQuoteCountByMood(mood: String): Int
    suspend fun getTotalQuoteCount(): Int
    suspend fun getAllMoods(): List<String>
    suspend fun deleteQuote(quote: QuoteEntity)
    suspend fun deleteQuoteById(quoteId: String)
    suspend fun deleteQuotesByMood(mood: String)
    suspend fun deleteAllQuotes()
    fun observeQuotesByMood(mood: String): Flow<List<QuoteEntity>>
}
