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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
fun JournalLogScreen(
    navController: NavController,
    viewModel: JournalLogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val themeColors = getThemeColors()

    val backgroundColor = themeColors.background
    val textColor = themeColors.onBackground
    val cardColor = themeColors.surface

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
                WaterDropLoading(tint = ColorFilter.tint(WaterBlue))
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
                contentPadding = PaddingValues(horizontal = ScreenUtils.responsivePadding() * 1.5f, vertical = ScreenUtils.responsivePadding()),
                verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding())
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
            .padding(
                start = if (ScreenUtils.isSmallScreen()) 16.dp else 24.dp,
                end = if (ScreenUtils.isSmallScreen()) 16.dp else 24.dp,
                top = 0.dp,
                bottom = if (ScreenUtils.isSmallScreen()) 6.dp else 8.dp
            ),
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
        modifier = Modifier.padding(vertical = ScreenUtils.responsiveSpacing())
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
        shape = RoundedCornerShape(ScreenUtils.responsiveSpacing() * 1.5f),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(ScreenUtils.responsivePadding()),
            verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing())
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
                        horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveTextSpacing())
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
                    Spacer(modifier = Modifier.width(ScreenUtils.responsiveTextSpacing() / 2))
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
                    horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                    modifier = Modifier.padding(top = ScreenUtils.responsiveTextSpacing())
                ) {
                    entry.tags.take(3).forEach { tag ->
                        Surface(
                            color = WaterBlue.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(ScreenUtils.responsiveSpacing() * 1.5f)
                        ) {
                            Text(
                                text = tag,
                                style = Typography.bodySmall.copy(
                                    color = WaterBlue,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(horizontal = ScreenUtils.responsiveSpacing(), vertical = ScreenUtils.responsiveTextSpacing())
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

        Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding()))

        Text(
            text = "Your journal is empty",
            style = Typography.titleMedium.copy(
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        )

        Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing()))

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
                verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding())
            ) {
                Text(
                    text = "Filter by Date",
                    style = Typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing())
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
