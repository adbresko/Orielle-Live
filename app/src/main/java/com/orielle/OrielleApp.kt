package com.orielle

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.jakewharton.threetenabp.AndroidThreeTen

@HiltAndroidApp
class OrielleApp : Application() {

    override fun onCreate() {
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        super.onCreate()

        // Initialize Timber
        Timber.plant(Timber.DebugTree())
        // Initialize ThreeTenABP
        AndroidThreeTen.init(this)

        // Global uncaught exception handler
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.e(throwable, "Uncaught exception in thread: ${thread.name}")
            FirebaseCrashlytics.getInstance().recordException(throwable)
        }
    }
}