package com.orielle.ui.screens.mood

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orielle.R
import androidx.compose.foundation.Image
import com.orielle.ui.components.OrielleScreenHeader

// Data for each mood
val moodReflectionData = mapOf(
    "Happy" to MoodReflectionUi(
        color = Color(0xFFFFF8E1),
        options = listOf("Grateful", "Inspired", "Proud", "Loved", "Connected", "Confident")
    ),
    "Sad" to MoodReflectionUi(
        color = Color(0xFFF7FAFC),
        options = listOf("Tired", "Lonely", "Let down", "Rejected", "Grieving", "Hopeless")
    ),
    "Angry" to MoodReflectionUi(
        color = Color(0xFFF7FAFC),
        options = listOf("Frustrated", "Unheard", "Irritated", "Judged", "Annoyed", "Controlled")
    ),
    "Surprised" to MoodReflectionUi(
        color = Color(0xFFF7FAFC),
        options = listOf("Shocked", "Confused", "Curious", "Alert", "Delighted", "Intrigued")
    ),
    "Playful" to MoodReflectionUi(
        color = Color(0xFFFFF8E1),
        options = listOf("Silly", "Creative", "Energetic", "Curious", "Alive", "Fresh")
    ),
    "Shy" to MoodReflectionUi(
        color = Color(0xFFF7FAFC),
        options = listOf("Insecure", "Vulnerable", "Hesitant", "Nervous", "Small", "Quiet")
    ),
    "Frustrated" to MoodReflectionUi(
        color = Color(0xFFFFF3F0),
        options = listOf("Blocked", "Tense", "Pressured", "Unseen", "Stuck", "Ignored")
    ),
    "Scared" to MoodReflectionUi(
        color = Color(0xFFF7FAFC),
        options = listOf("Unsafe", "Anxious", "Nervous", "Trapped", "Alone", "Frozen")
    ),
    "Peaceful" to MoodReflectionUi(
        color = Color(0xFFFFF8E1),
        options = listOf("Calm", "Grounded", "Present", "Safe", "Aligned", "Relaxed")
    )
)

data class MoodReflectionUi(val color: Color, val options: List<String>)

@Composable
fun MoodReflectionScreen(
    moodName: String,
    moodIconRes: Int,
    onSave: (String, List<String>, String) -> Unit,
    onBack: () -> Unit
) {
    val reflectionUi = moodReflectionData[moodName] ?: moodReflectionData["Happy"]!!
    var selectedOptions by remember { mutableStateOf(listOf<String>()) }
    var notes by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(reflectionUi.color)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        OrielleScreenHeader(
            text = "You're feeling $moodName"
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3F2FD)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = moodIconRes),
                contentDescription = moodName,
                modifier = Modifier.size(80.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "What might be behind this feeling?",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        ReflectionOptionsGrid(
            options = reflectionUi.options,
            selected = selectedOptions,
            onSelect = { option ->
                selectedOptions = if (selectedOptions.contains(option)) {
                    selectedOptions - option
                } else {
                    selectedOptions + option
                }
            }
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp),
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text("Anything else  you’d like to express?") },
            textStyle = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { onSave(moodName, selectedOptions, notes.text) },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8EC6C6))
        ) {
            Text("Save & Continue", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "← Back to moods",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF888888),
            modifier = Modifier
                .clickable { onBack() }
                .padding(8.dp)
        )
    }
}

@Composable
private fun ReflectionOptionsGrid(
    options: List<String>,
    selected: List<String>,
    onSelect: (String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        options.chunked(3).forEach { rowOptions ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                rowOptions.forEach { option ->
                    ReflectionOptionChip(
                        text = option,
                        selected = selected.contains(option),
                        onClick = { onSelect(option) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReflectionOptionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) Color(0xFF8EC6C6) else Color.White,
        border = if (selected) null else BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .clickable { onClick() }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (selected) Color.White else Color(0xFF444444),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
        )
    }
}