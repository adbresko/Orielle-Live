package com.orielle.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.orielle.data.local.dao.ChatMessageDao
import com.orielle.data.local.dao.JournalDao
import com.orielle.data.local.dao.MoodCheckInDao
import com.orielle.data.local.dao.MoodTagDao
import com.orielle.data.local.dao.UserDao
import com.orielle.data.local.model.JournalEntryEntity
import com.orielle.data.local.model.MoodCheckInEntity
import com.orielle.data.local.model.UserEntity
import com.orielle.data.local.model.ChatMessageEntity
import com.orielle.data.local.model.MoodTagEntity

@Database(
    entities = [
        UserEntity::class,
        JournalEntryEntity::class,
        MoodCheckInEntity::class,
        ChatMessageEntity::class,
        MoodTagEntity::class
    ],
    version = 4, // Increment version for new entities
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class OrielleDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun journalDao(): JournalDao
    abstract fun moodCheckInDao(): MoodCheckInDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun moodTagDao(): MoodTagDao
}
