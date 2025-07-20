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
    version = 4, // Increment the version because we added new UserEntity fields
    exportSchema = false
)
@TypeConverters(Converters::class) // Add this to handle the Date type
abstract class OrielleDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun journalDao(): JournalDao
    abstract fun moodCheckInDao(): MoodCheckInDao

    companion object {
        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Add new columns to users table
                database.execSQL("ALTER TABLE users ADD COLUMN firstName TEXT")
                database.execSQL("ALTER TABLE users ADD COLUMN lastName TEXT")
                database.execSQL("ALTER TABLE users ADD COLUMN premium INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE users ADD COLUMN createdAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
                database.execSQL("ALTER TABLE users ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT ${System.currentTimeMillis()}")
            }
        }
    }
}
