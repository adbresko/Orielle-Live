package com.orielle.ui.screens.mood

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.orielle.R
import com.orielle.ui.theme.OrielleTheme
import androidx.compose.foundation.Image
import com.orielle.ui.components.OrielleScreenHeader
import com.orielle.ui.components.AccountRequiredModal
import com.orielle.ui.screens.auth.AuthViewModel
import com.orielle.ui.util.ScreenUtils
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.ui.platform.LocalContext
import android.os.Vibrator
import android.os.VibratorManager
import android.content.Context

@Composable
fun MoodCheckInScreen(
    viewModel: MoodCheckInViewModel = hiltViewModel(),
    onMoodSelected: (String) -> Unit = {},
    onSkip: () -> Unit = {}
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val isUserAuthenticated by authViewModel.isUserAuthenticated.collectAsState()
    val isGuest = isUserAuthenticated == false
    var showAccountModal by remember { mutableStateOf(false) }
    var selectedMood by remember { mutableStateOf<String?>(null) }

    // Color mapping for mood backgrounds
    val moodColors = mapOf(
        "Happy" to Color(0xFFFFF9E6),
        "Sad" to Color(0xFFF2F6F8),
        "Angry" to Color(0xFFF0F2F5),
        "Frustrated" to Color(0xFFFFF4F2),
        "Scared" to Color(0xFFF6F5F9),
        "Surprised" to Color(0xFFF8F7FC),
        "Playful" to Color(0xFFFFFCEC),
        "Shy" to Color(0xFFF9F5F8),
        "Peaceful" to Color(0xFFFEFBF0)
    )

    // Animate background color based on selected mood
    val backgroundColor by animateColorAsState(
        targetValue = selectedMood?.let { moodColors[it] } ?: MaterialTheme.colorScheme.background,
        animationSpec = tween(durationMillis = 300), label = "background"
    )

    // Show modal if guest
    if (showAccountModal) {
        AccountRequiredModal(
            onSignUp = { showAccountModal = false /* TODO: Navigate to sign up */ },
            onCancel = { showAccountModal = false }
        )
    }

    if (isGuest) {
        // Trigger modal if guest
        LaunchedEffect(Unit) { showAccountModal = true }
    }

    val uiState by viewModel.uiState.collectAsState()
    val userName = uiState.userName ?: "User"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(ScreenUtils.responsivePadding() * 1.5f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OrielleScreenHeader(
            text = "How is your inner weather, $userName?"
        )
        Text(
            text = "Tap on the weather that feels most like you.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF222222),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = ScreenUtils.responsivePadding() * 2)
        )

        // Emotion Selection Grid (3x3)
        EmotionGrid(
            onMoodSelected = { mood ->
                selectedMood = mood
                viewModel.saveMoodCheckIn(mood)
                // Remove automatic navigation - only update UI state
            }
        )

        Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 3))

        // Show Continue button when mood is selected
        if (selectedMood != null) {
            Button(
                onClick = {
                    // Only navigate when Continue button is clicked
                    onMoodSelected(selectedMood!!)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing() * 1.5f))
        }
        // Always show Skip for now option under Continue or in original place
        Text(
            text = "Skip for now",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier
                .clickable { onSkip() }
                .padding(ScreenUtils.responsiveSpacing())
                .align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun EmotionGrid(
    onMoodSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val vibrator = remember {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }
    val emotions = listOf(
        EmotionData("Surprised", R.drawable.ic_surprised, Color(0xFFE1BEE7)),
        EmotionData("Happy", R.drawable.ic_happy, Color(0xFFFFF59D)),
        EmotionData("Sad", R.drawable.ic_sad, Color(0xFF90A4AE)),
        EmotionData("Playful", R.drawable.ic_playful, Color(0xFFFFF59D)),
        EmotionData("Angry", R.drawable.ic_angry, Color(0xFF90A4AE)),
        EmotionData("Shy", R.drawable.ic_shy, Color(0xFFE1BEE7)),
        EmotionData("Frustrated", R.drawable.ic_frustrated, Color(0xFFFFAB91)),
        EmotionData("Scared", R.drawable.ic_scared, Color(0xFFB3E5FC)),
        EmotionData("Peaceful", R.drawable.ic_peaceful, Color(0xFFFFF59D))
    )
    var selectedMood by remember { mutableStateOf<String?>(null) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding())
    ) {
        // Row 1
        Row(
            horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding())
        ) {
            emotions.take(3).forEach { emotion ->
                EmotionButton(
                    emotion = emotion,
                    selected = selectedMood == emotion.name,
                    selectedMood = selectedMood,
                    onClick = {
                        // Trigger haptic feedback
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(50)
                        }
                        selectedMood = emotion.name
                        onMoodSelected(emotion.name)
                    }
                )
            }
        }
        // Row 2
        Row(
            horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding())
        ) {
            emotions.slice(3..5).forEach { emotion ->
                EmotionButton(
                    emotion = emotion,
                    selected = selectedMood == emotion.name,
                    selectedMood = selectedMood,
                    onClick = {
                        // Trigger haptic feedback
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(50)
                        }
                        selectedMood = emotion.name
                        onMoodSelected(emotion.name)
                    }
                )
            }
        }
        // Row 3
        Row(
            horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding())
        ) {
            emotions.takeLast(3).forEach { emotion ->
                EmotionButton(
                    emotion = emotion,
                    selected = selectedMood == emotion.name,
                    selectedMood = selectedMood,
                    onClick = {
                        // Trigger haptic feedback
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            vibrator.vibrate(android.os.VibrationEffect.createOneShot(50, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(50)
                        }
                        selectedMood = emotion.name
                        onMoodSelected(emotion.name)
                    }
                )
            }
        }
    }
}

@Composable
private fun EmotionButton(
    emotion: EmotionData,
    selected: Boolean = false,
    selectedMood: String?,
    onClick: () -> Unit
) {
    // Pop animation for selected icon
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = tween(durationMillis = 120), label = "pop"
    )
    // Opacity logic: before any selection, all are 1f; after, only selected is 1f, others 0.5f
    val targetOpacity = if (selectedMood == null) 1f else if (selected) 1f else 0.5f
    val opacity by animateFloatAsState(
        targetValue = targetOpacity,
        animationSpec = tween(durationMillis = 300), label = "fade"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .graphicsLayer {
                    scaleX = scale;
                    scaleY = scale;
                    alpha = opacity
                }
                .clip(CircleShape)
                .clickable(
                    onClick = onClick,
                    indication = rememberRipple(bounded = true),
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = emotion.color.copy(alpha = 0.2f),
                tonalElevation = 0.dp,
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = emotion.iconRes),
                    contentDescription = emotion.name,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = emotion.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = opacity),
            fontWeight = FontWeight.Medium
        )
    }
}

private data class EmotionData(
    val name: String,
    val iconRes: Int,
    val color: Color
)

@Preview(showBackground = true)
@Composable
private fun MoodCheckInScreenPreview() {
    OrielleTheme {
        MoodCheckInScreen()
    }
}