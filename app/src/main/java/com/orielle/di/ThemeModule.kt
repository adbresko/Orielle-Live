package com.orielle.di

import android.content.Context
import com.orielle.ui.theme.ThemeManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ThemeModule {

    @Provides
    @Singleton
    fun provideThemeManager(
        @ApplicationContext context: Context
    ): ThemeManager {
        return ThemeManager(context)
    }
}
