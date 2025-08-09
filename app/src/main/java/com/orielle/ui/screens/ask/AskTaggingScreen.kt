package com.orielle.ui.screens.ask

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.orielle.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskTaggingScreen(
    navController: NavController,
    viewModel: AskTaggingViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = MaterialTheme.colorScheme.background == DarkGray
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    var newTagText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        // Header
        TopAppBar(
            title = {
                Text(
                    text = "Tag Your Conversation",
                    style = Typography.headlineLarge,
                    color = textColor
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Description
        Text(
            text = "Add tags to help you find this conversation later. You can select from suggestions or create your own.",
            style = Typography.bodyLarge,
            color = if (isDark) SoftSand.copy(alpha = 0.8f) else Charcoal.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Suggested tags
        if (uiState.suggestedTags.isNotEmpty()) {
            Text(
                text = "Suggested Tags",
                style = Typography.titleMedium,
                color = textColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.suggestedTags) { tag ->
                    SuggestedTagChip(
                        tag = tag,
                        isSelected = uiState.selectedTags.contains(tag),
                        onToggle = { viewModel.toggleSuggestedTag(tag) },
                        isDark = isDark
                    )
                }
            }
        }

        // Selected tags
        if (uiState.selectedTags.isNotEmpty()) {
            Text(
                text = "Your Tags",
                style = Typography.titleMedium,
                color = textColor,
                modifier = Modifier.padding(bottom = 12.dp, top = 16.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(uiState.selectedTags) { tag ->
                    SelectedTagChip(
                        tag = tag,
                        onRemove = { viewModel.removeTag(tag) },
                        isDark = isDark
                    )
                }
            }
        }

        // Add custom tag input
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) DarkGray else SoftSand
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isDark) 0.dp else 2.dp
            )
        ) {
            TextField(
                value = newTagText,
                onValueChange = { newTagText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Add a custom tag...",
                        style = Typography.bodyLarge,
                        color = if (isDark) SoftSand.copy(alpha = 0.6f) else Charcoal.copy(alpha = 0.6f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = if (isDark) SoftSand else Charcoal,
                    unfocusedTextColor = if (isDark) SoftSand else Charcoal
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (newTagText.isNotBlank()) {
                            viewModel.addCustomTag(newTagText.trim())
                            newTagText = ""
                        }
                    }
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Save button
        Button(
            onClick = {
                viewModel.saveConversation()
                navController.navigate("home_graph") {
                    popUpTo("ask_tagging") { inclusive = true }
                }
                // TODO: Show toast "Your conversation with Orielle was saved."
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = StillwaterTeal
            ),
            enabled = uiState.selectedTags.isNotEmpty()
        ) {
            Text(
                text = "Save Conversation",
                style = Typography.labelLarge,
                color = Color.White,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
fun SuggestedTagChip(
    tag: String,
    isSelected: Boolean,
    onToggle: () -> Unit,
    isDark: Boolean
) {
    FilterChip(
        onClick = onToggle,
        label = {
            Text(
                text = tag,
                style = Typography.bodyMedium,
                color = if (isSelected) Color.White else if (isDark) SoftSand else Charcoal
            )
        },
        selected = isSelected,
        enabled = true,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = if (isDark) DarkGray else SoftSand,
            selectedContainerColor = WaterBlue,
            labelColor = if (isDark) SoftSand else Charcoal,
            selectedLabelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = if (isDark) SoftSand.copy(alpha = 0.3f) else Charcoal.copy(alpha = 0.3f),
            selectedBorderColor = WaterBlue,
            enabled = true,
            selected = isSelected
        )
    )
}

@Composable
fun SelectedTagChip(
    tag: String,
    onRemove: () -> Unit,
    isDark: Boolean
) {
    AssistChip(
        onClick = { },
        label = {
            Text(
                text = tag,
                style = Typography.bodyMedium,
                color = Color.White
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove tag",
                tint = Color.White,
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove() }
            )
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = WaterBlue,
            labelColor = Color.White
        ),
        border = null
    )
}

data class AskTaggingUiState(
    val suggestedTags: List<String> = listOf(
        "anxiety", "relationships", "work", "family", "self-care",
        "gratitude", "growth", "challenges", "emotions", "mindfulness"
    ),
    val selectedTags: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AskTaggingViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AskTaggingUiState())
    val uiState: StateFlow<AskTaggingUiState> = _uiState.asStateFlow()

    fun toggleSuggestedTag(tag: String) {
        val currentTags = _uiState.value.selectedTags
        val newTags = if (currentTags.contains(tag)) {
            currentTags - tag
        } else {
            currentTags + tag
        }
        _uiState.value = _uiState.value.copy(selectedTags = newTags)
    }

    fun addCustomTag(tag: String) {
        val currentTags = _uiState.value.selectedTags
        if (!currentTags.contains(tag)) {
            _uiState.value = _uiState.value.copy(selectedTags = currentTags + tag)
        }
    }

    fun removeTag(tag: String) {
        val currentTags = _uiState.value.selectedTags
        _uiState.value = _uiState.value.copy(selectedTags = currentTags - tag)
    }

    fun saveConversation() {
        // TODO: Implement actual saving logic
        // This would save the conversation with the selected tags
    }
}

@Preview(name = "Ask Tagging Screen - Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_AskTaggingScreen_Light() {
    val fakeNavController = rememberNavController()
    OrielleTheme(darkTheme = false) {
        AskTaggingScreen(navController = fakeNavController)
    }
}

@Preview(name = "Ask Tagging Screen - Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun Preview_AskTaggingScreen_Dark() {
    val fakeNavController = rememberNavController()
    OrielleTheme(darkTheme = true) {
        AskTaggingScreen(navController = fakeNavController)
    }
}

@Preview(name = "Suggested Tag Chip - Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_SuggestedTagChip_Light() {
    OrielleTheme(darkTheme = false) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SuggestedTagChip(
                tag = "anxiety",
                isSelected = false,
                onToggle = {},
                isDark = false
            )
            SuggestedTagChip(
                tag = "growth",
                isSelected = true,
                onToggle = {},
                isDark = false
            )
        }
    }
}

@Preview(name = "Suggested Tag Chip - Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun Preview_SuggestedTagChip_Dark() {
    OrielleTheme(darkTheme = true) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SuggestedTagChip(
                tag = "anxiety",
                isSelected = false,
                onToggle = {},
                isDark = true
            )
            SuggestedTagChip(
                tag = "growth",
                isSelected = true,
                onToggle = {},
                isDark = true
            )
        }
    }
}

@Preview(name = "Selected Tag Chip - Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_SelectedTagChip_Light() {
    OrielleTheme(darkTheme = false) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectedTagChip(
                tag = "mindfulness",
                onRemove = {},
                isDark = false
            )
            SelectedTagChip(
                tag = "self-care",
                onRemove = {},
                isDark = false
            )
        }
    }
}

@Preview(name = "Selected Tag Chip - Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun Preview_SelectedTagChip_Dark() {
    OrielleTheme(darkTheme = true) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            SelectedTagChip(
                tag = "mindfulness",
                onRemove = {},
                isDark = true
            )
            SelectedTagChip(
                tag = "self-care",
                onRemove = {},
                isDark = true
            )
        }
    }
}
