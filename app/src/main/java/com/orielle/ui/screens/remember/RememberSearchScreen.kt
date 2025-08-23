package com.orielle.ui.screens.remember

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import com.orielle.ui.theme.*
import com.orielle.domain.model.UserActivity
import com.orielle.domain.model.ActivityType
import java.util.Date

@Composable
fun RememberSearchScreen(
    navController: NavController,
    viewModel: RememberSearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadSearchData()
    }

    Scaffold(
        topBar = {
            SearchTopBar(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search Input
            SearchInput(
                query = uiState.searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Filter Sections
            FilterSection(
                selectedTypes = uiState.selectedTypes,
                selectedMoods = uiState.selectedMoods,
                selectedTags = uiState.selectedTags,
                availableMoods = uiState.availableMoods,
                availableTags = uiState.availableTags,
                onTypeToggle = { viewModel.toggleTypeFilter(it) },
                onMoodToggle = { viewModel.toggleMoodFilter(it) },
                onTagToggle = { viewModel.toggleTagFilter(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Results Section
            if (uiState.filteredResults.isEmpty() && (uiState.searchQuery.isNotEmpty() ||
                        uiState.selectedTypes.isNotEmpty() || uiState.selectedMoods.isNotEmpty() ||
                        uiState.selectedTags.isNotEmpty())) {
                EmptySearchResults()
            } else {
                ResultsList(
                    results = uiState.filteredResults,
                    onResultClick = { activity ->
                        when (activity.activityType) {
                            ActivityType.REFLECT -> navController.navigate("journal_detail/${activity.relatedId}")
                            ActivityType.ASK -> navController.navigate("conversation_detail/${activity.relatedId}")
                            ActivityType.CHECK_IN -> navController.navigate("mood_detail")
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopBar(
    onNavigateBack: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Filter & Search",
                fontFamily = Lora,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun SearchInput(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = getCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Charcoal,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = NotoSans,
                        fontSize = 14.sp,
                        color = Charcoal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (query.isEmpty()) {
                    Text(
                        text = "Search by tag, mood, or memory...",
                        fontFamily = NotoSans,
                        fontSize = 14.sp,
                        color = Color(0xFF999999),
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            if (query.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = Color(0xFF999999),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    selectedTypes: Set<ActivityType>,
    selectedMoods: Set<String>,
    selectedTags: Set<String>,
    availableMoods: List<String>,
    availableTags: List<String>,
    onTypeToggle: (ActivityType) -> Unit,
    onMoodToggle: (String) -> Unit,
    onTagToggle: (String) -> Unit
) {
    Column {
        // Type Filter
        FilterGroup(
            title = "Type",
            content = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TypeFilterChip(
                        type = ActivityType.REFLECT,
                        isSelected = selectedTypes.contains(ActivityType.REFLECT),
                        onClick = { onTypeToggle(ActivityType.REFLECT) }
                    )
                    TypeFilterChip(
                        type = ActivityType.ASK,
                        isSelected = selectedTypes.contains(ActivityType.ASK),
                        onClick = { onTypeToggle(ActivityType.ASK) }
                    )
                    TypeFilterChip(
                        type = ActivityType.CHECK_IN,
                        isSelected = selectedTypes.contains(ActivityType.CHECK_IN),
                        onClick = { onTypeToggle(ActivityType.CHECK_IN) }
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Mood Filter
        if (availableMoods.isNotEmpty()) {
            FilterGroup(
                title = "Mood",
                content = {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableMoods) { mood ->
                            MoodFilterChip(
                                mood = mood,
                                isSelected = selectedMoods.contains(mood),
                                onClick = { onMoodToggle(mood) }
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Tags Filter
        if (availableTags.isNotEmpty()) {
            FilterGroup(
                title = "Tags",
                content = {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(availableTags) { tag ->
                            TagFilterChip(
                                tag = tag,
                                isSelected = selectedTags.contains(tag),
                                onClick = { onTagToggle(tag) }
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun FilterGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            fontFamily = NotoSans,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Charcoal,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun TypeFilterChip(
    type: ActivityType,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (text, color) = when (type) {
        ActivityType.REFLECT -> "Reflection" to StillwaterTeal
        ActivityType.ASK -> "Chat" to AuroraGold
        ActivityType.CHECK_IN -> "Check-in" to WaterBlue
    }

    Card(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.15f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, color)
        else
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Text(
            text = text,
            fontFamily = NotoSans,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) color else Charcoal,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun MoodFilterChip(
    mood: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) WaterBlue else Color(0xFFE0E0E0),
                shape = CircleShape
            )
            .background(
                color = if (isSelected) WaterBlue.copy(alpha = 0.1f) else Color.White,
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = mood.take(2).uppercase(),
            fontFamily = NotoSans,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) WaterBlue else Charcoal,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TagFilterChip(
    tag: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) WaterBlue.copy(alpha = 0.15f) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, WaterBlue)
        else
            androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Text(
            text = tag,
            fontFamily = NotoSans,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) WaterBlue else Charcoal,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ResultsList(
    results: List<UserActivity>,
    onResultClick: (UserActivity) -> Unit
) {
    if (results.isEmpty()) {
        return
    }

    Text(
        text = "Results (${results.size})",
        fontFamily = NotoSans,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        color = Charcoal,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(results) { activity ->
            SearchResultCard(
                activity = activity,
                onClick = { onResultClick(activity) }
            )
        }
    }
}

@Composable
private fun SearchResultCard(
    activity: UserActivity,
    onClick: () -> Unit
) {
    val activityColor = when (activity.activityType) {
        ActivityType.REFLECT -> StillwaterTeal
        ActivityType.ASK -> AuroraGold
        ActivityType.CHECK_IN -> WaterBlue
    }

    val activityTypeText = when (activity.activityType) {
        ActivityType.REFLECT -> "Reflection"
        ActivityType.ASK -> "Chat"
        ActivityType.CHECK_IN -> "Check-in"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with type and date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(activityColor, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = activityTypeText,
                        fontFamily = NotoSans,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = activityColor
                    )
                }

                Text(
                    text = formatDate(activity.timestamp),
                    fontFamily = NotoSans,
                    fontSize = 12.sp,
                    color = Color(0xFF999999)
                )
            }

            // Title
            if (activity.title != null) {
                Text(
                    text = activity.title,
                    fontFamily = NotoSans,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Charcoal,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Preview content
            if (activity.preview != null) {
                Text(
                    text = activity.preview,
                    fontFamily = NotoSans,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    maxLines = 3,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Tags
            if (activity.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    activity.tags.take(3).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(
                                    color = activityColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tag,
                                fontFamily = NotoSans,
                                fontSize = 11.sp,
                                color = activityColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (activity.tags.size > 3) {
                        Text(
                            text = "+${activity.tags.size - 3}",
                            fontFamily = NotoSans,
                            fontSize = 11.sp,
                            color = Color(0xFF999999),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            // Mood for check-ins
            if (activity.activityType == ActivityType.CHECK_IN && activity.mood != null) {
                Text(
                    text = "Mood: ${activity.mood}",
                    fontFamily = NotoSans,
                    fontSize = 12.sp,
                    color = WaterBlue,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptySearchResults() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No results found",
            fontFamily = NotoSans,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Try adjusting your search or filters",
            fontFamily = NotoSans,
            fontSize = 14.sp,
            color = Color(0xFF999999),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// Helper function to format date
private fun formatDate(date: Date): String {
    val calendar = java.util.Calendar.getInstance()
    calendar.time = date

    val today = java.util.Calendar.getInstance()
    val yesterday = java.util.Calendar.getInstance()
    yesterday.add(java.util.Calendar.DAY_OF_YEAR, -1)

    return when {
        calendar.get(java.util.Calendar.YEAR) == today.get(java.util.Calendar.YEAR) &&
                calendar.get(java.util.Calendar.DAY_OF_YEAR) == today.get(java.util.Calendar.DAY_OF_YEAR) -> "Today"

        calendar.get(java.util.Calendar.YEAR) == yesterday.get(java.util.Calendar.YEAR) &&
                calendar.get(java.util.Calendar.DAY_OF_YEAR) == yesterday.get(java.util.Calendar.DAY_OF_YEAR) -> "Yesterday"

        else -> {
            val month = calendar.getDisplayName(java.util.Calendar.MONTH, java.util.Calendar.SHORT, java.util.Locale.getDefault())
            val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            "$month $day"
        }
    }
}

@Preview(name = "Remember Search Screen", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun RememberSearchScreenPreview() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftSand)
    ) {
        SearchTopBar(
            onNavigateBack = {}
        )

        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            SearchInput(
                query = "",
                onQueryChange = {}
            )

            Spacer(modifier = Modifier.height(24.dp))

            FilterSection(
                selectedTypes = setOf(ActivityType.REFLECT),
                selectedMoods = setOf("Happy"),
                selectedTags = setOf("growth"),
                availableMoods = listOf("Happy", "Sad", "Peaceful", "Excited"),
                availableTags = listOf("growth", "mindfulness", "relationships", "work"),
                onTypeToggle = {},
                onMoodToggle = {},
                onTagToggle = {}
            )
        }
    }
}
