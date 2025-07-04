package com.orielle

import android.app.Application
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class OrielleApp : Application() {

    override fun onCreate() {

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        super.onCreate()
    }
}