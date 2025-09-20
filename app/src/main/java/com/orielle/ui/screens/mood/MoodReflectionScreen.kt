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
import com.orielle.ui.util.ScreenUtils
import androidx.compose.ui.unit.sp
import com.orielle.R
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.alpha
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
    val maxSelection = 3
    val selectionLimitReached = selectedOptions.size >= maxSelection

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(reflectionUi.color)
            .padding(horizontal = ScreenUtils.responsivePadding() * 1.5f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 3))
        OrielleScreenHeader(
            text = "You're feeling $moodName"
        )
        Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding()))
        Box(
            modifier = Modifier
                .size(ScreenUtils.responsiveImageSize(120.dp))
                .clip(CircleShape)
                .background(Color(0xFFE3F2FD)),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = moodIconRes),
                contentDescription = moodName,
                modifier = Modifier.size(ScreenUtils.responsiveImageSize(80.dp))
            )
        }
        Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 1.5f))
        Text(
            text = "What might be behind this feeling?",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )
        if (selectionLimitReached) {
            Text(
                text = "You can only select up to 3.",
                color = Color(0xFFB00020),
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = ScreenUtils.responsiveTextSpacing(), bottom = ScreenUtils.responsiveSpacing())
            )
        } else {
            Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing() * 1.5f))
        }
        ReflectionOptionsGrid(
            options = reflectionUi.options,
            selected = selectedOptions,
            selectionLimitReached = selectionLimitReached,
            onSelect = { option ->
                selectedOptions = if (selectedOptions.contains(option)) {
                    selectedOptions - option
                } else {
                    if (selectedOptions.size < maxSelection) selectedOptions + option else selectedOptions
                }
            }
        )
        Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 1.5f))
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
        Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding()))
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
    selectionLimitReached: Boolean,
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
                    val isSelected = selected.contains(option)
                    val isEnabled = isSelected || !selectionLimitReached
                    ReflectionOptionChip(
                        text = option,
                        selected = isSelected,
                        enabled = isEnabled,
                        onClick = { if (isEnabled) onSelect(option) }
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
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        selected -> Color(0xFF8EC6C6)
        !enabled -> Color(0xFFF0F0F0)
        else -> Color.White
    }
    val contentColor = when {
        selected -> Color.White
        !enabled -> Color(0xFFAAAAAA)
        else -> Color(0xFF444444)
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = backgroundColor,
        border = if (selected) null else BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .alpha(if (enabled) 1f else 0.5f)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
        )
    }
}