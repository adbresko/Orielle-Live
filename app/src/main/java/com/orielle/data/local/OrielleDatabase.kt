// path: app/src/main/java/com/orielle/data/local/OrielleDatabase.kt
package com.orielle.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.orielle.data.local.dao.UserDao
import com.orielle.data.local.model.UserEntity

@Database(
    entities = [UserEntity::class],
    version = 1,
    exportSchema = false
)
abstract class OrielleDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}