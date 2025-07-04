package com.orielle.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.orielle.data.local.dao.JournalDao
import com.orielle.data.local.dao.UserDao
import com.orielle.data.local.model.JournalEntryEntity
import com.orielle.data.local.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        JournalEntryEntity::class // Add the new entity here
    ],
    version = 2, // Increment the version because we changed the schema
    exportSchema = false
)
@TypeConverters(Converters::class) // Add this to handle the Date type
abstract class OrielleDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun journalDao(): JournalDao // Add the new DAO abstract function

}
