package com.orielle.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.orielle.data.local.dao.JournalDao
import com.orielle.data.local.dao.MoodCheckInDao
import com.orielle.data.local.dao.UserDao
import com.orielle.data.local.model.JournalEntryEntity
import com.orielle.data.local.model.MoodCheckInEntity
import com.orielle.data.local.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        JournalEntryEntity::class,
        MoodCheckInEntity::class
    ],
    version = 3, // Increment the version because we added mood check-ins
    exportSchema = false
)
@TypeConverters(Converters::class) // Add this to handle the Date type
abstract class OrielleDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun journalDao(): JournalDao
    abstract fun moodCheckInDao(): MoodCheckInDao

}
