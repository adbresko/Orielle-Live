package com.orielle.data.local.dao

import androidx.room.*
import com.orielle.data.local.model.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: QuoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<QuoteEntity>)

    @Query("SELECT * FROM quotes WHERE mood = :mood ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuoteByMood(mood: String): QuoteEntity?

    @Query("SELECT * FROM quotes WHERE mood = :mood")
    suspend fun getAllQuotesByMood(mood: String): List<QuoteEntity>

    @Query("SELECT * FROM quotes")
    suspend fun getAllQuotes(): List<QuoteEntity>

    @Query("SELECT COUNT(*) FROM quotes WHERE mood = :mood")
    suspend fun getQuoteCountByMood(mood: String): Int

    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun getTotalQuoteCount(): Int

    @Query("SELECT DISTINCT mood FROM quotes ORDER BY mood")
    suspend fun getAllMoods(): List<String>

    @Delete
    suspend fun deleteQuote(quote: QuoteEntity)

    @Query("DELETE FROM quotes WHERE id = :quoteId")
    suspend fun deleteQuoteById(quoteId: String)

    @Query("DELETE FROM quotes WHERE mood = :mood")
    suspend fun deleteQuotesByMood(mood: String)

    @Query("DELETE FROM quotes")
    suspend fun deleteAllQuotes()

    // For delta sync - get all quote IDs to compare with CSV
    @Query("SELECT id FROM quotes")
    suspend fun getAllQuoteIds(): List<String>

    // For avoiding repeated quotes - track recently shown quotes
    @Query("SELECT * FROM quotes WHERE mood = :mood AND id NOT IN (:excludeIds) ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomQuoteByMoodExcluding(mood: String, excludeIds: List<String>): QuoteEntity?

    @Query("SELECT * FROM quotes WHERE mood = :mood AND id NOT IN (:excludeIds) ORDER BY RANDOM() LIMIT :limit")
    suspend fun getRandomQuotesByMoodExcluding(mood: String, excludeIds: List<String>, limit: Int): List<QuoteEntity>
}
