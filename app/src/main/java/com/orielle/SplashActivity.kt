package com.orielle

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.orielle.ui.theme.OrielleTheme
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OrielleTheme {
                // The Splash Activity now only shows one screen: the combined video and text.
                VideoBrandingScreen {
                    navigateToMainApp()
                }
            }
        }
    }

    private fun navigateToMainApp() {
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Important: remove SplashActivity from the back stack
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoBrandingScreen(onExperienceEnd: () -> Unit) {
    // Immediately navigate to main app, skipping the video
    LaunchedEffect(Unit) {
        onExperienceEnd()
    }

    // Simple loading screen while transitioning
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "ORIELLE",
            style = MaterialTheme.typography.displayMedium,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}
