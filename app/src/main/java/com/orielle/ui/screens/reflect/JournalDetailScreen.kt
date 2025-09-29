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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
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
private fun FontSizeAdjuster(
    currentFontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    textColor: Color,
    cardColor: Color
) {
    val minFontSize = 12f
    val maxFontSize = 28f
    val fontSizeStep = 2f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenUtils.responsivePadding()),
        shape = RoundedCornerShape(ScreenUtils.responsivePadding()),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScreenUtils.responsivePadding()),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Font size label
            Text(
                text = "Text Size",
                fontFamily = NotoSans,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )

            // Font size controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Decrease button
                IconButton(
                    onClick = {
                        val newSize = (currentFontSize - fontSizeStep).coerceAtLeast(minFontSize)
                        onFontSizeChange(newSize)
                    },
                    enabled = currentFontSize > minFontSize,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (currentFontSize > minFontSize)
                                WaterBlue.copy(alpha = 0.1f)
                            else
                                Color.Transparent
                        )
                        .border(
                            width = 1.dp,
                            color = if (currentFontSize > minFontSize) WaterBlue else textColor.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Decrease text size",
                        tint = if (currentFontSize > minFontSize) WaterBlue else textColor.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Current font size display
                Text(
                    text = "${currentFontSize.toInt()}sp",
                    fontFamily = NotoSans,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = WaterBlue,
                    modifier = Modifier.padding(horizontal = ScreenUtils.responsiveSpacing())
                )

                // Increase button
                IconButton(
                    onClick = {
                        val newSize = (currentFontSize + fontSizeStep).coerceAtMost(maxFontSize)
                        onFontSizeChange(newSize)
                    },
                    enabled = currentFontSize < maxFontSize,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (currentFontSize < maxFontSize)
                                WaterBlue.copy(alpha = 0.1f)
                            else
                                Color.Transparent
                        )
                        .border(
                            width = 1.dp,
                            color = if (currentFontSize < maxFontSize) WaterBlue else textColor.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Increase text size",
                        tint = if (currentFontSize < maxFontSize) WaterBlue else textColor.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

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

    // Font size state for reading experience
    var currentFontSize by remember { mutableStateOf(16f) }

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
                    currentFontSize = currentFontSize,
                    onFontSizeChange = { currentFontSize = it },
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
    currentFontSize: Float,
    onFontSizeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding() * 1.5f)
    ) {
        // Font Size Adjuster
        FontSizeAdjuster(
            currentFontSize = currentFontSize,
            onFontSizeChange = onFontSizeChange,
            textColor = textColor,
            cardColor = cardColor
        )

        // Prompt section (if applicable)
        if (entry.promptText != null && entry.promptText.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(ScreenUtils.responsivePadding() * 1.25f),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(ScreenUtils.responsivePadding() * 1.5f),
                    verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding())
                ) {
                    Text(
                        text = "Prompt",
                        style = Typography.bodySmall.copy(
                            color = textColor.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = entry.promptText?.replace("+", " ") ?: "",
                        fontFamily = Lora,
                        fontSize = (currentFontSize * 1.5).sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        lineHeight = (currentFontSize * 1.8).sp
                    )
                }
            }
        }

        // Main content - simple text display
        Text(
            text = entry.content,
            style = Typography.bodyLarge.copy(
                color = textColor,
                fontSize = currentFontSize.sp,
                lineHeight = (currentFontSize * 1.5).sp
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

@Preview(showBackground = true, name = "Journal Detail - Light", backgroundColor = 0xFFF6F5F1)
@Composable
fun JournalDetailScreenLightPreview() {
    val fakeThemeManager = com.orielle.ui.theme.ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    com.orielle.ui.theme.OrielleThemeWithPreference(themeManager = fakeThemeManager) {
        JournalDetailScreen(
            navController = rememberNavController(),
            entryId = "preview_entry"
        )
    }
}

@Preview(showBackground = true, name = "Journal Detail - Dark", backgroundColor = 0xFF1A1A1A)
@Composable
fun JournalDetailScreenDarkPreview() {
    val fakeThemeManager = com.orielle.ui.theme.ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    com.orielle.ui.theme.OrielleTheme(darkTheme = true) {
        JournalDetailScreen(
            navController = rememberNavController(),
            entryId = "preview_entry"
        )
    }
}
