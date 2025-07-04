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
    val context = LocalContext.current

    // Animation states for the different text elements
    var brandTextVisible by remember { mutableStateOf(false) }
    val brandTextAlpha by animateFloatAsState(
        targetValue = if (brandTextVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "brand text fade"
    )

    var arrivalTextVisible by remember { mutableStateOf(false) }
    val arrivalTextAlpha by animateFloatAsState(
        targetValue = if (arrivalTextVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 1500),
        label = "arrival text fade"
    )

    // Trigger animations with delays
    LaunchedEffect(Unit) {
        delay(500)
        brandTextVisible = true
        delay(1000) // Wait a bit longer for the arrival text
        arrivalTextVisible = true
    }

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri("android.resource://${context.packageName}/${R.raw.brand_intro}")
            setMediaItem(mediaItem)
            playWhenReady = true
            prepare()
        }
    }

    DisposableEffect(exoPlayer) {
        val listener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    onExperienceEnd()
                }
            }
        }
        exoPlayer.addListener(listener)
        onDispose {
            exoPlayer.removeListener(listener)
            exoPlayer.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Video Player Background
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // "ORIELLE" Text Overlay (Centered)
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ORIELLE",
                style = MaterialTheme.typography.displayMedium,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.alpha(brandTextAlpha)
            )
        }

        // "Arrival" Text and Skip Button Overlay (Bottom)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp, start = 24.dp, end = 24.dp)
                .alpha(arrivalTextAlpha), // Fade in the whole column
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "You belong in this moment.",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Take a breath.",
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))
            TextButton(onClick = onExperienceEnd) {
                Text(
                    "Skip",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
