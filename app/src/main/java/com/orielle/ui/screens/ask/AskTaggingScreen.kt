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
import com.orielle.ui.util.ScreenUtils
import androidx.navigation.NavController
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.orielle.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskTaggingScreen(
    navController: NavController,
    conversationId: String? = null,
    tempEntryId: String? = null,
    viewModel: AskTaggingViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {

    // Set conversation ID in ViewModel
    LaunchedEffect(conversationId) {
        conversationId?.let { viewModel.setConversationId(it) }
    }

    // Set temp entry ID for journal entries
    LaunchedEffect(tempEntryId) {
        tempEntryId?.let { viewModel.setTempEntryId(it) }
    }
    val uiState by viewModel.uiState.collectAsState()
    val themeColors = getThemeColors()
    val backgroundColor = themeColors.background
    val textColor = themeColors.onBackground

    var newTagText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(ScreenUtils.responsivePadding())
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

        Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 1.5f))

        // Description
        Text(
            text = "Add tags to help you find this conversation later. You can select from suggestions or create your own.",
            style = Typography.bodyLarge,
            color = themeColors.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.padding(bottom = ScreenUtils.responsivePadding() * 1.5f)
        )

        // Suggested tags
        if (uiState.suggestedTags.isNotEmpty()) {
            Text(
                text = "Suggested Tags",
                style = Typography.titleMedium,
                color = textColor,
                modifier = Modifier.padding(bottom = ScreenUtils.responsiveSpacing() * 1.5f)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                contentPadding = PaddingValues(bottom = ScreenUtils.responsivePadding())
            ) {
                items(uiState.suggestedTags) { tag ->
                    SuggestedTagChip(
                        tag = tag,
                        isSelected = uiState.selectedTags.contains(tag),
                        onToggle = { viewModel.toggleSuggestedTag(tag) },
                        isDark = themeColors.isDark
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
                modifier = Modifier.padding(bottom = ScreenUtils.responsiveSpacing() * 1.5f, top = ScreenUtils.responsivePadding())
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                contentPadding = PaddingValues(bottom = ScreenUtils.responsivePadding())
            ) {
                items(uiState.selectedTags) { tag ->
                    SelectedTagChip(
                        tag = tag,
                        onRemove = { viewModel.removeTag(tag) },
                        isDark = themeColors.isDark
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
                containerColor = themeColors.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (themeColors.isDark) 0.dp else 2.dp
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
                        color = themeColors.onBackground.copy(alpha = 0.6f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = themeColors.onBackground,
                    unfocusedTextColor = themeColors.onBackground
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
                viewModel.saveConversation(navController)
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
    val themeColors = getThemeColors()
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
            containerColor = themeColors.surface,
            selectedContainerColor = WaterBlue,
            labelColor = themeColors.onBackground,
            selectedLabelColor = Color.White
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = themeColors.onBackground.copy(alpha = 0.3f),
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
    val themeColors = getThemeColors()
    AssistChip(
        onClick = { },
        label = {
            Text(
                text = tag,
                style = Typography.bodyMedium,
                color = themeColors.onBackground
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove tag",
                tint = themeColors.onBackground,
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
class AskTaggingViewModel @Inject constructor(
    private val chatRepository: com.orielle.domain.repository.ChatRepository,
    private val sessionManager: com.orielle.domain.manager.SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AskTaggingUiState())
    val uiState: StateFlow<AskTaggingUiState> = _uiState.asStateFlow()

    private var conversationId: String? = null
    private var tempEntryId: String? = null

    fun setConversationId(id: String) {
        conversationId = id
    }

    fun setTempEntryId(id: String) {
        tempEntryId = id
    }

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

    fun saveConversation(navController: NavController) {
        val convId = conversationId
        val entryId = tempEntryId

        if (convId == null && entryId == null) {
            Timber.e("No conversation ID or entry ID set for saving")
            return
        }

        viewModelScope.launch {
            try {
                // If this is for a journal entry, just navigate back with tags
                if (entryId != null) {
                    // For journal entries, we'll pass the tags back via navigation
                    // The JournalEditorViewModel will handle updating the tags
                    val tagsString = _uiState.value.selectedTags.joinToString(",")
                    navController.navigate("journal_editor?tags=$tagsString") {
                        popUpTo("journal_editor") { inclusive = true }
                    }
                    return@launch
                }
                _uiState.value = _uiState.value.copy(isLoading = true)

                // Update conversation saved status
                val savedResult = chatRepository.updateConversationSavedStatus(convId.toString(), true)
                when (savedResult) {
                    is com.orielle.domain.model.Response.Success -> {
                        Timber.d("Successfully marked conversation as saved: $convId")

                        // TODO: Also update conversation with tags
                        // This would require an updateConversationTags method in the repository

                    }
                    is com.orielle.domain.model.Response.Failure -> {
                        Timber.e(savedResult.exception, "Failed to save conversation")
                        _uiState.value = _uiState.value.copy(
                            error = "Failed to save conversation: ${savedResult.exception?.message ?: "Unknown error"}"
                        )
                    }
                    else -> {
                        Timber.e("Unknown response type when saving conversation")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in saveConversation")
                _uiState.value = _uiState.value.copy(
                    error = "Error saving conversation: ${e.message}"
                )
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
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
