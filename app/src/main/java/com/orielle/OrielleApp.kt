package com.orielle

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * The main Application class for Orielle.
 *
 * This class is the entry point for the application process.
 * The @HiltAndroidApp annotation triggers Hilt's code generation, including a base class
 * for the application that serves as the application-level dependency container.
 */
@HiltAndroidApp
class OrielleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Application-level initialization code can go here in the future.
    }
}
