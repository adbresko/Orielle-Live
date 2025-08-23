package com.orielle.data.local.dao

import androidx.room.*
import com.orielle.data.local.model.MemoryEntryEntity
import com.orielle.data.local.model.EntryType
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * DAO for memory entries
 */
@Dao
interface MemoryEntryDao {

    @Query("SELECT * FROM memory_entries WHERE userId = :userId ORDER BY date DESC, createdAt DESC")
    fun getAllMemoryEntries(userId: String): Flow<List<MemoryEntryEntity>>

    @Query("SELECT * FROM memory_entries WHERE userId = :userId AND date = :date ORDER BY createdAt DESC")
    fun getMemoryEntriesForDate(userId: String, date: Date): Flow<List<MemoryEntryEntity>>

    @Query("SELECT * FROM memory_entries WHERE userId = :userId AND entryType = :entryType ORDER BY date DESC, createdAt DESC")
    fun getMemoryEntriesByType(userId: String, entryType: EntryType): Flow<List<MemoryEntryEntity>>

    @Query("SELECT * FROM memory_entries WHERE id = :id")
    suspend fun getMemoryEntryById(id: String): MemoryEntryEntity?

    @Query("""
        SELECT * FROM memory_entries 
        WHERE userId = :userId 
        AND (
            content LIKE '%' || :searchQuery || '%' 
            OR mood LIKE '%' || :searchQuery || '%'
            OR tags LIKE '%' || :searchQuery || '%'
        )
        ORDER BY date DESC, createdAt DESC
    """)
    fun searchMemoryEntries(userId: String, searchQuery: String): Flow<List<MemoryEntryEntity>>

    @Query("""
        SELECT * FROM memory_entries 
        WHERE userId = :userId 
        AND entryType IN (:entryTypes)
        AND (:mood IS NULL OR mood = :mood)
        AND (
            :searchQuery IS NULL OR
            content LIKE '%' || :searchQuery || '%' 
            OR mood LIKE '%' || :searchQuery || '%'
            OR tags LIKE '%' || :searchQuery || '%'
        )
        ORDER BY date DESC, createdAt DESC
    """)
    fun filterMemoryEntries(
        userId: String,
        entryTypes: List<EntryType>,
        mood: String? = null,
        searchQuery: String? = null
    ): Flow<List<MemoryEntryEntity>>

    @Query("""
        SELECT DISTINCT date FROM memory_entries 
        WHERE userId = :userId 
        AND date >= :startDate 
        AND date <= :endDate
    """)
    suspend fun getDatesWithEntries(userId: String, startDate: Date, endDate: Date): List<Date>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemoryEntry(memoryEntry: MemoryEntryEntity)

    @Update
    suspend fun updateMemoryEntry(memoryEntry: MemoryEntryEntity)

    @Delete
    suspend fun deleteMemoryEntry(memoryEntry: MemoryEntryEntity)

    @Query("DELETE FROM memory_entries WHERE id = :id")
    suspend fun deleteMemoryEntryById(id: String)
}
