package com.orielle.data.service

import android.content.Context
import com.orielle.data.repository.QuoteRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service responsible for initializing quotes on app startup.
 * This ensures quotes are loaded from CSV and synced with the database.
 */
@Singleton
class QuoteInitializationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val quoteRepository: QuoteRepository,
) {
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    /**
     * Initialize quotes on app startup.
     * This should be called from the Application class or MainActivity.
     */
    fun initializeQuotes() {
        serviceScope.launch {
            try {
                val result = quoteRepository.initializeQuotes()
                if (result.isSuccess) {
                    // Quotes initialized successfully
                    Timber.tag("QuoteInitialization").d("Quotes initialized successfully")
                } else {
                    // Handle initialization error
                    Timber.tag("QuoteInitialization")
                        .e(result.exceptionOrNull(), "Failed to initialize quotes")
                }
            } catch (e: Exception) {
                Timber.tag("QuoteInitialization").e(e, "Error during quote initialization")
            }
        }
    }
}
