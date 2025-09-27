package com.orielle

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.jakewharton.threetenabp.AndroidThreeTen
import com.orielle.data.service.QuoteInitializationService
import javax.inject.Inject

@HiltAndroidApp
class OrielleApp : Application() {

    @Inject
    lateinit var quoteInitializationService: QuoteInitializationService

    override fun onCreate() {
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        super.onCreate()

        // Initialize Timber
        Timber.plant(Timber.DebugTree())
        // Initialize ThreeTenABP
        AndroidThreeTen.init(this)

        // Initialize quotes
        quoteInitializationService.initializeQuotes()

        // Global uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.e(throwable, "Uncaught exception in thread: ${thread.name}")
            FirebaseCrashlytics.getInstance().recordException(throwable)
        }
    }
}