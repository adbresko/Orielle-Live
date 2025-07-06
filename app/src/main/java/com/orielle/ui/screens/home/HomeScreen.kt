package com.orielle.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.orielle.R
import com.orielle.ui.theme.OrielleTheme

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedMood by remember { mutableStateOf<Mood?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(top = 48.dp, bottom = 48.dp)
        ) {
            item {
                val welcomeMessage = if (uiState.isGuest) {
                    "Your temporary sanctuary"
                } else {
                    "Welcome home"
                }
                Text(
                    text = welcomeMessage,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- The new Mood Arc Gauge Card ---
            item {
                MoodArcGaugeCard(moods = allMoods)
                Spacer(modifier = Modifier.height(24.dp))
            }

            // We can keep this for comparison or remove it later
            item {
                MoodReflectionCard(
                    moods = allMoods,
                    selectedMood = selectedMood,
                    onMoodSelected = { selectedMood = it }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                ReflectionCenterCard()
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (!uiState.isGuest) {
                item {
                    GardenCard()
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

// --- Other component cards remain the same ---
@Composable
private fun MoodReflectionCard(
    moods: List<Mood>,
    selectedMood: Mood?,
    onMoodSelected: (Mood) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "AI Inspired mood reflection card",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            val chunkedMoods = moods.chunked(4)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                chunkedMoods.forEach { rowMoods ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        rowMoods.forEach { mood ->
                            MoodIconButton(
                                mood = mood,
                                isSelected = mood == selectedMood,
                                onClick = { onMoodSelected(mood) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodIconButton(
    mood: Mood,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.aspectRatio(1f),
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected) mood.color else MaterialTheme.colorScheme.surface,
        onClick = onClick,
        tonalElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = mood.iconResId),
                contentDescription = mood.name,
                modifier = Modifier.fillMaxSize(0.6f),
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


@Composable
private fun ReflectionCenterCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Reflection Center", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Based on your selection and weather patterns",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { /* TODO: Navigate to Ask Orielle */ },
                shape = MaterialTheme.shapes.medium,
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text("Ask Orielle")
            }
        }
    }
}

@Composable
private fun GardenCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Daily Activity", style = MaterialTheme.typography.labelMedium)
                Text(
                    "37212",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.ic_orielle_drop),
                contentDescription = "Growth",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                Text("Growth", style = MaterialTheme.typography.labelMedium)
                Text(
                    "8638",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun HomeScreenThematicPreview() {
    OrielleTheme {
        HomeScreen()
    }
}