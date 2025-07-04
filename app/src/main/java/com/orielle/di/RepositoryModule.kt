package com.orielle.di

import com.orielle.data.repository.AuthRepository
import com.orielle.data.repository.AuthRepositoryImpl
import com.orielle.data.repository.JournalRepository
import com.orielle.data.repository.JournalRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    /**
     * Binds the JournalRepositoryImpl to the JournalRepository interface.
     * This tells Hilt that whenever a JournalRepository is requested,
     * it should provide an instance of JournalRepositoryImpl.
     */
    @Binds
    @Singleton
    abstract fun bindJournalRepository(
        journalRepositoryImpl: JournalRepositoryImpl
    ): JournalRepository
}
