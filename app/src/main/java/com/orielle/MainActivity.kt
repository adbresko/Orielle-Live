package com.orielle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.orielle.ui.navigation.AppNavigation
import com.orielle.ui.theme.OrielleTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OrielleTheme {
                // Set up the navigation host for the entire application
                AppNavigation()
            }
        }
    }
}
