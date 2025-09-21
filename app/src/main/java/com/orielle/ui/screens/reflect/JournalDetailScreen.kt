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
import com.orielle.ui.util.ScreenUtils
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
    val themeColors = getThemeColors()

    val backgroundColor = themeColors.background
    val textColor = themeColors.onBackground
    val cardColor = themeColors.surface

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
            uiState.entry?.let { entry ->
                JournalDetailTopBar(
                    entry = entry,
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
                        .padding(ScreenUtils.responsivePadding() * 1.5f)
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
                        verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding())
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
    entry: JournalEntry,
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
            .padding(start = ScreenUtils.responsivePadding(), end = ScreenUtils.responsivePadding() * 1.5f, top = ScreenUtils.responsiveSpacing(), bottom = ScreenUtils.responsiveSpacing()),
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

        // Center content - Date and Location
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
            Text(
                text = dateFormat.format(entry.timestamp),
                style = Typography.titleLarge.copy(
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            )

            if (entry.location != null) {
                Text(
                    text = entry.location,
                    style = Typography.bodyMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.Normal
                    )
                )
            }
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
        verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding() * 1.5f)
    ) {
        // Main content - simple text display
        Text(
            text = entry.content,
            style = Typography.bodyLarge.copy(
                color = textColor,
                lineHeight = 24.sp
            )
        )

        // Tags if available
        if (entry.tags.isNotEmpty()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing() * 1.5f)
            ) {
                Text(
                    text = "Tags",
                    style = Typography.bodyMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.Bold
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    entry.tags.forEach { tag ->
                        Card(
                            shape = RoundedCornerShape(ScreenUtils.responsivePadding()),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Text(
                                text = tag,
                                style = Typography.bodySmall.copy(
                                    color = textColor,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = ScreenUtils.responsiveSpacing() * 1.5f, vertical = ScreenUtils.responsiveTextSpacing() * 1.5f)
                            )
                        }
                    }
                }
            }
        }
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
