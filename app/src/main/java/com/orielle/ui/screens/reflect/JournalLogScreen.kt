package com.orielle.ui.screens.reflect

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.orielle.domain.model.JournalEntry
import com.orielle.domain.model.JournalEntryType
import com.orielle.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JournalLogScreen(
    navController: NavController,
    viewModel: JournalLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = !MaterialTheme.colorScheme.background.equals(SoftSand)

    val backgroundColor = if (isDark) DarkGray else SoftSand
    val textColor = if (isDark) SoftSand else Charcoal
    val cardColor = if (isDark) Color(0xFF2A2A2A) else Color.White

    // Filter Dialog
    if (uiState.showFilterDialog) {
        FilterDialog(
            onDismiss = { viewModel.hideFilterDialog() },
            onApplyFilter = { dateFilter, tagFilter ->
                viewModel.applyFilter(dateFilter, tagFilter)
                viewModel.hideFilterDialog()
            }
        )
    }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            JournalLogTopBar(
                textColor = textColor,
                onBackClick = { navController.popBackStack() },
                onFilterClick = { viewModel.showFilterDialog() }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = WaterBlue)
            }
        } else if (uiState.filteredEntries.isEmpty()) {
            EmptyJournalState(
                textColor = textColor,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val groupedEntries = uiState.filteredEntries.groupBy { entry ->
                    SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(entry.timestamp)
                }

                groupedEntries.forEach { (date, entries) ->
                    item {
                        DateHeader(
                            date = date,
                            textColor = textColor
                        )
                    }

                    items(entries) { entry ->
                        JournalEntryCard(
                            entry = entry,
                            cardColor = cardColor,
                            textColor = textColor,
                            onClick = { navController.navigate("journal_detail/${entry.id}") }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalLogTopBar(
    textColor: Color,
    onBackClick: () -> Unit,
    onFilterClick: () -> Unit
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

        Text(
            text = "Your Journal",
            style = Typography.headlineSmall.copy(
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        )

        IconButton(onClick = onFilterClick) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = textColor
            )
        }
    }
}

@Composable
private fun DateHeader(
    date: String,
    textColor: Color
) {
    Text(
        text = date,
        style = Typography.titleMedium.copy(
            color = textColor,
            fontWeight = FontWeight.SemiBold
        ),
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun JournalEntryCard(
    entry: JournalEntry,
    cardColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header with location and time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (entry.location != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "📍",
                            fontSize = 12.sp
                        )
                        Text(
                            text = entry.location,
                            style = Typography.bodySmall.copy(
                                color = textColor.copy(alpha = 0.7f)
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Text(
                    text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(entry.timestamp),
                    style = Typography.bodySmall.copy(
                        color = textColor.copy(alpha = 0.7f)
                    )
                )
            }

            // Content preview
            Text(
                text = entry.content.take(120) + if (entry.content.length > 120) "..." else "",
                style = Typography.bodyMedium.copy(
                    color = textColor,
                    lineHeight = 20.sp
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Tags (if any)
            if (entry.tags.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    entry.tags.take(3).forEach { tag ->
                        Surface(
                            color = WaterBlue.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = tag,
                                style = Typography.bodySmall.copy(
                                    color = WaterBlue,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    if (entry.tags.size > 3) {
                        Text(
                            text = "+${entry.tags.size - 3}",
                            style = Typography.bodySmall.copy(
                                color = textColor.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyJournalState(
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "📔",
            fontSize = 48.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Your journal is empty",
            style = Typography.titleMedium.copy(
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start reflecting on your thoughts and experiences",
            style = Typography.bodyMedium.copy(
                color = textColor.copy(alpha = 0.7f)
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun FilterDialog(
    onDismiss: () -> Unit,
    onApplyFilter: (dateFilter: String?, tagFilter: String?) -> Unit
) {
    var selectedDateFilter by remember { mutableStateOf<String?>(null) }
    var selectedTagFilter by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Entries") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Filter by Date",
                    style = Typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedDateFilter == "today",
                        onClick = {
                            selectedDateFilter = if (selectedDateFilter == "today") null else "today"
                        },
                        label = { Text("Today") }
                    )

                    FilterChip(
                        selected = selectedDateFilter == "week",
                        onClick = {
                            selectedDateFilter = if (selectedDateFilter == "week") null else "week"
                        },
                        label = { Text("This Week") }
                    )

                    FilterChip(
                        selected = selectedDateFilter == "month",
                        onClick = {
                            selectedDateFilter = if (selectedDateFilter == "month") null else "month"
                        },
                        label = { Text("This Month") }
                    )
                }

                // TODO: Add tag filter options based on existing tags
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onApplyFilter(selectedDateFilter, selectedTagFilter) }
            ) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun JournalLogScreenLightPreview() {
    OrielleTheme(darkTheme = false) {
        JournalLogScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun JournalLogScreenDarkPreview() {
    OrielleTheme(darkTheme = true) {
        JournalLogScreen(navController = rememberNavController())
    }
}
