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
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
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
                .padding(bottom = 32.dp)
        )

        // Emotion Selection Grid (3x3)
        EmotionGrid(
            onMoodSelected = { mood ->
                viewModel.saveMoodCheckIn(mood)
                onMoodSelected(mood)
            }
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Skip for now option
        Text(
            text = "Skip for now",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            modifier = Modifier
                .clickable { onSkip() }
                .padding(8.dp)
        )
    }
}

@Composable
private fun EmotionGrid(
    onMoodSelected: (String) -> Unit
) {
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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Row 1
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            emotions.take(3).forEach { emotion ->
                EmotionButton(
                    emotion = emotion,
                    onClick = { onMoodSelected(emotion.name) }
                )
            }
        }
        // Row 2
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            emotions.slice(3..5).forEach { emotion ->
                EmotionButton(
                    emotion = emotion,
                    onClick = { onMoodSelected(emotion.name) }
                )
            }
        }
        // Row 3
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            emotions.takeLast(3).forEach { emotion ->
                EmotionButton(
                    emotion = emotion,
                    onClick = { onMoodSelected(emotion.name) }
                )
            }
        }
    }
}

@Composable
private fun EmotionButton(
    emotion: EmotionData,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
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
            color = MaterialTheme.colorScheme.onBackground,
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