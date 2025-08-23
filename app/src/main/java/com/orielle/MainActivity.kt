package com.orielle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.orielle.ui.navigation.AppNavigation
import com.orielle.ui.theme.OrielleThemeWithPreference
import com.orielle.ui.theme.ThemeManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge but handle insets properly
        enableEdgeToEdge()

        // Ensure the status bar and navigation bar don't interfere
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // Use ThemeManager to observe and apply theme preference
            OrielleThemeWithPreference(themeManager = themeManager) {
                // Wrap navigation in a Scaffold that handles system bars
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        themeManager = themeManager
                    )
                }
            }
        }
    }
}
