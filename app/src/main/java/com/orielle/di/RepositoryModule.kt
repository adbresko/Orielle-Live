package com.orielle.di

import com.orielle.data.repository.AuthRepository
import com.orielle.data.repository.AuthRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * Binds the AuthRepositoryImpl to the AuthRepository interface.
     * This tells Hilt that whenever an AuthRepository is requested,
     * it should provide an instance of AuthRepositoryImpl.
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository
}
