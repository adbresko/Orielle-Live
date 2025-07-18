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

@Composable
fun MoodCheckInScreen(
    viewModel: MoodCheckInViewModel = hiltViewModel(),
    onMoodSelected: (String) -> Unit = {},
    onSkip: () -> Unit = {}
) {
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
        // Header/Question
        Text(
            text = "How is your inner weather, $userName?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(bottom = 48.dp)
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
        EmotionData("Happy", R.drawable.ic_happy, Color(0xFFFFF59D)), // Using existing clear icon for happy
        EmotionData("Sad", R.drawable.ic_sad, Color(0xFF90A4AE)), // Using existing foggy icon for sad
        EmotionData("Playful", R.drawable.ic_playful, Color(0xFFFFF59D)),
        EmotionData("Angry", R.drawable.ic_angry, Color(0xFF90A4AE)),
        EmotionData("Shy", R.drawable.ic_shy, Color(0xFFE1BEE7)), // Using existing partly cloudy for shy
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
        Surface(
            shape = CircleShape,
            color = emotion.color.copy(alpha = 0.2f),
            modifier = Modifier
                .size(80.dp)
                .clickable(
                    onClick = onClick,
                    indication = rememberRipple(bounded = true),
                    interactionSource = remember { MutableInteractionSource() }
                ),
            tonalElevation = 0.dp
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(id = emotion.iconRes),
                    contentDescription = emotion.name,
                    modifier = Modifier.size(48.dp),
                    tint = emotion.color
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