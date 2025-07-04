// path: app/src/main/java/com/orielle/di/DatabaseModule.kt
package com.orielle.di

import android.content.Context
import androidx.room.Room
import com.orielle.data.local.OrielleDatabase
import com.orielle.data.local.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideOrielleDatabase(@ApplicationContext context: Context): OrielleDatabase {
        return Room.databaseBuilder(
            context,
            OrielleDatabase::class.java,
            "orielle_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    @Singleton
    fun provideUserDao(database: OrielleDatabase): UserDao {
        return database.userDao()
    }
}