package com.orielle.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date
import java.util.*

/**
 * Room entity for storing all user memories (reflections and conversations)
 */
@Entity(tableName = "memory_entries")
@TypeConverters(MemoryEntryConverters::class)
data class MemoryEntryEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val userId: String,
    val date: Date,
    val entryType: EntryType,
    val mood: String?,
    val content: String,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Type of memory entry
 */
enum class EntryType {
    REFLECT,    // Journal/reflection entries
    ASK         // Chat/conversation entries
}

/**
 * Type converters for Room database
 */
class MemoryEntryConverters {

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    @TypeConverter
    fun fromEntryType(entryType: EntryType): String {
        return entryType.name
    }

    @TypeConverter
    fun toEntryType(entryTypeString: String): EntryType {
        return EntryType.valueOf(entryTypeString)
    }

    @TypeConverter
    fun fromTagsList(tags: List<String>): String {
        return Gson().toJson(tags)
    }

    @TypeConverter
    fun toTagsList(tagsString: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(tagsString, type) ?: emptyList()
    }
}
