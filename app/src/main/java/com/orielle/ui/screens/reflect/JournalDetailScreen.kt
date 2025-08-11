package com.orielle.ui.screens.reflect

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.orielle.domain.model.JournalEntry
import com.orielle.domain.model.JournalEntryType
import com.orielle.ui.theme.*
import com.orielle.ui.components.WaterDropLoading
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalDetailScreen(
    navController: NavController,
    entryId: String,
    viewModel: JournalDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = !MaterialTheme.colorScheme.background.equals(SoftSand)

    val backgroundColor = if (isDark) DarkGray else SoftSand
    val textColor = if (isDark) SoftSand else Charcoal
    val cardColor = if (isDark) Color(0xFF2A2A2A) else Color.White

    // Load entry on startup
    LaunchedEffect(entryId) {
        viewModel.loadEntry(entryId)
    }

    // Handle delete confirmation dialog
    if (uiState.showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteDialog() },
            title = { Text("Delete Entry?") },
            text = { Text("Are you sure you want to delete this journal entry? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteEntry {
                            navController.popBackStack()
                        }
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Show options menu
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            JournalDetailTopBar(
                textColor = textColor,
                onBackClick = { navController.popBackStack() },
                onMoreClick = { showMenu = true },
                showMenu = showMenu,
                onDismissMenu = { showMenu = false },
                onEditClick = {
                    showMenu = false
                    navController.navigate("journal_editor?entryId=$entryId")
                },
                onDeleteClick = {
                    showMenu = false
                    viewModel.showDeleteDialog()
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                WaterDropLoading(tint = ColorFilter.tint(WaterBlue))
            }
        } else {
            uiState.entry?.let { entry ->
                JournalDetailContent(
                    entry = entry,
                    cardColor = cardColor,
                    textColor = textColor,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp)
                )
            } ?: run {
                // Entry not found
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📝",
                            fontSize = 48.sp
                        )
                        Text(
                            text = "Entry not found",
                            style = Typography.titleMedium.copy(
                                color = textColor,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalDetailTopBar(
    textColor: Color,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    showMenu: Boolean,
    onDismissMenu: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = textColor
            )
        }

        Box {
            IconButton(onClick = onMoreClick) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = textColor
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = onDismissMenu
            ) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    onClick = onEditClick,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = onDeleteClick,
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun JournalDetailContent(
    entry: JournalEntry,
    cardColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Metadata Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date and time
                val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                Text(
                    text = dateFormat.format(entry.timestamp),
                    style = Typography.titleMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Text(
                    text = timeFormat.format(entry.timestamp),
                    style = Typography.bodyMedium.copy(
                        color = textColor.copy(alpha = 0.7f)
                    )
                )

                // Location if available
                if (entry.location != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = textColor.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = entry.location,
                            style = Typography.bodyMedium.copy(
                                color = textColor.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }
        }

        // Prompt text if this was a prompt-based entry
        if (entry.promptText != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = WaterBlue.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "✨ Prompt",
                        style = Typography.titleSmall.copy(
                            color = WaterBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = entry.promptText,
                        style = Typography.bodyMedium.copy(
                            color = textColor,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }

        // Main content
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = entry.content,
                modifier = Modifier.padding(20.dp),
                style = Typography.bodyLarge.copy(
                    color = textColor,
                    lineHeight = 24.sp
                )
            )
        }

        // Tags if available
        if (entry.tags.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tags",
                        style = Typography.titleSmall.copy(
                            color = textColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        entry.tags.forEach { tag ->
                            Surface(
                                color = WaterBlue.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = tag,
                                    style = Typography.bodySmall.copy(
                                        color = WaterBlue,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Entry type indicator
        val entryTypeText = when (entry.entryType) {
            JournalEntryType.PROMPT -> "Prompt-based reflection"
            JournalEntryType.FREE_WRITE -> "Free writing"
            JournalEntryType.QUICK_ENTRY -> "Quick thought"
        }

        Text(
            text = entryTypeText,
            style = Typography.bodySmall.copy(
                color = textColor.copy(alpha = 0.5f)
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
fun JournalDetailScreenLightPreview() {
    OrielleTheme(darkTheme = false) {
        JournalDetailScreen(
            navController = rememberNavController(),
            entryId = "preview_entry"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun JournalDetailScreenDarkPreview() {
    OrielleTheme(darkTheme = true) {
        JournalDetailScreen(
            navController = rememberNavController(),
            entryId = "preview_entry"
        )
    }
}
