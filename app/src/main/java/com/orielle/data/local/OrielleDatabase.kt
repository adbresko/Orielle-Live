package com.orielle.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.orielle.data.local.dao.JournalDao
import com.orielle.data.local.dao.MoodCheckInDao
import com.orielle.data.local.dao.UserDao
import com.orielle.data.local.dao.ChatConversationDao
import com.orielle.data.local.dao.ChatMessageDao
import com.orielle.data.local.dao.TagDao
import com.orielle.data.local.dao.MemoryEntryDao
import com.orielle.data.local.dao.QuoteDao
import com.orielle.data.local.dao.JournalPromptDao
import com.orielle.data.local.model.JournalEntryEntity
import com.orielle.data.local.model.MoodCheckInEntity
import com.orielle.data.local.model.UserEntity
import com.orielle.data.local.model.ChatConversationEntity
import com.orielle.data.local.model.ChatMessageEntity
import com.orielle.data.local.model.TagEntity
import com.orielle.data.local.model.ConversationTagCrossRef
import com.orielle.data.local.model.MemoryEntryEntity
import com.orielle.data.local.model.QuoteEntity
import com.orielle.data.local.model.JournalPromptEntity

@Database(
    entities = [
        UserEntity::class,
        JournalEntryEntity::class,
        MoodCheckInEntity::class,
        ChatConversationEntity::class,
        ChatMessageEntity::class,
        TagEntity::class,
        ConversationTagCrossRef::class,
        MemoryEntryEntity::class,
        QuoteEntity::class,
        JournalPromptEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OrielleDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun journalDao(): JournalDao
    abstract fun moodCheckInDao(): MoodCheckInDao
    abstract fun chatConversationDao(): ChatConversationDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun tagDao(): TagDao
    abstract fun memoryEntryDao(): MemoryEntryDao
    abstract fun quoteDao(): QuoteDao
    abstract fun journalPromptDao(): JournalPromptDao

    companion object {
        // No migrations needed - Room will create tables from entities
    }
}
