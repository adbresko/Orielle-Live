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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.min
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import com.orielle.ui.theme.*
import com.orielle.domain.model.UserActivity
import com.orielle.domain.model.ActivityType
import com.orielle.R
import com.orielle.ui.util.ScreenUtils
import com.orielle.ui.components.WaterDropLoading
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
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            SearchTopBar(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WaterDropLoading(
                        size = ScreenUtils.responsiveImageSize(60.dp).value.toInt(),
                        modifier = Modifier.size(ScreenUtils.responsiveImageSize(60.dp))
                    )
                    Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing() * 2))
                    Text(
                        text = "Loading your memories...",
                        style = Typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(horizontal = ScreenUtils.responsivePadding()),
                contentPadding = PaddingValues(top = 8.dp, bottom = ScreenUtils.responsiveIconSize()),
                verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing())
            ) {
                item {
                    // Search Input
                    SearchInput(
                        query = uiState.searchQuery,
                        onQueryChange = { viewModel.updateSearchQuery(it) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding()))
                }

                item {
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
                }

                item {
                    Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding()))
                }

                // Results Section
                if (uiState.filteredResults.isEmpty() && (uiState.searchQuery.isNotEmpty() ||
                            uiState.selectedTypes.isNotEmpty() || uiState.selectedMoods.isNotEmpty() ||
                            uiState.selectedTags.isNotEmpty())) {
                    item {
                        EmptySearchResults()
                    }
                } else if (uiState.filteredResults.isNotEmpty()) {
                    item {
                        Text(
                            text = "Results (${uiState.filteredResults.size})",
                            fontFamily = NotoSans,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    items(uiState.filteredResults) { activity ->
                        SearchResultCard(
                            activity = activity,
                            onClick = {
                                when (activity.activityType) {
                                    ActivityType.REFLECT -> navController.navigate("journal_detail/${activity.relatedId}")
                                    ActivityType.ASK -> navController.navigate("conversation_detail/${activity.relatedId}")
                                    ActivityType.CHECK_IN -> { /* Ignore mood check-ins */ }
                                }
                            }
                        )
                    }
                }
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
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
                .padding(horizontal = ScreenUtils.responsivePadding(), vertical = ScreenUtils.responsiveSpacing()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(ScreenUtils.responsiveIconSize())
            )

            Spacer(modifier = Modifier.width(ScreenUtils.responsiveSpacing()))

            Box(modifier = Modifier.weight(1f)) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = NotoSans,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (query.isEmpty()) {
                    Text(
                        text = "Search by tag, mood, or memory...",
                        fontFamily = NotoSans,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            if (query.isNotEmpty()) {
                Spacer(modifier = Modifier.width(ScreenUtils.responsiveSpacing()))
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(ScreenUtils.responsiveIconSize(ScreenUtils.responsiveIconSize()))
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(ScreenUtils.responsiveIconSize(ScreenUtils.responsivePadding()))
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
                    horizontalArrangement = Arrangement.spacedBy(
                        (min(LocalConfiguration.current.screenWidthDp, LocalConfiguration.current.screenHeightDp) * 0.03f).dp.coerceIn(8.dp, ScreenUtils.responsivePadding())
                    )
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
                }
            }
        )

        Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding()))

        // Mood Filter
        if (availableMoods.isNotEmpty()) {
            FilterGroup(
                title = "Mood",
                content = {
                    // Create a 3x3 grid layout for mood icons
                    val moodChunks = availableMoods.chunked(3)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing())
                    ) {
                        moodChunks.forEach { moodRow ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                moodRow.forEach { mood ->
                                    Box(modifier = Modifier.weight(1f)) {
                                        MoodFilterChip(
                                            mood = mood,
                                            isSelected = selectedMoods.contains(mood),
                                            onClick = { onMoodToggle(mood) }
                                        )
                                    }
                                }
                                // Fill remaining space if row has less than 3 items
                                repeat(3 - moodRow.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding()))
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
            color = MaterialTheme.colorScheme.onBackground,
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
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val baseSize = min(screenWidth.value, screenHeight.value).dp

    // Responsive sizing for type chips
    val fontSize = (baseSize.value * 0.035f).coerceIn(12f, 16f).sp
    val horizontalPadding = (baseSize.value * 0.04f).coerceIn(12f, 20f).dp
    val verticalPadding = (baseSize.value * 0.02f).coerceIn(6f, 10f).dp
    val cornerRadius = (baseSize.value * 0.05f).coerceIn(16f, 24f).dp

    val (text, color) = when (type) {
        ActivityType.REFLECT -> "Reflection" to StillwaterTeal
        ActivityType.ASK -> "Chat" to AuroraGold
        ActivityType.CHECK_IN -> "Check-in" to WaterBlue // Fallback (shouldn't be displayed)
    }

    Surface(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(cornerRadius.value),
        color = if (isSelected) color else Color.Transparent,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Text(
            text = text,
            fontFamily = NotoSans,
            fontSize = fontSize,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding)
        )
    }
}

@Composable
private fun getResponsiveSizes(): Pair<Dp, Dp> {
    return ScreenUtils.getResponsiveSizes()
}

@Composable
private fun MoodFilterChip(
    mood: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (baseContainerSize, baseIconSize) = getResponsiveSizes()
    // Increase sizes slightly using dynamic scaling
    val containerSize = baseContainerSize * 1.15f
    val iconSize = baseIconSize * 1.15f

    // Color mapping similar to mood check-in screen
    val moodColors = mapOf(
        "happy" to Color(0xFFFFF59D),
        "sad" to Color(0xFF90A4AE),
        "angry" to Color(0xFF90A4AE),
        "frustrated" to Color(0xFFFFAB91),
        "scared" to Color(0xFFB3E5FC),
        "surprised" to Color(0xFFE1BEE7),
        "playful" to Color(0xFFFFF59D),
        "shy" to Color(0xFFE1BEE7),
        "peaceful" to Color(0xFFFFF59D)
    )

    val backgroundColor = moodColors[mood.lowercase()] ?: Color(0xFFF5F5F5)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(containerSize)
                .clip(CircleShape)
                .border(
                    width = if (isSelected) (containerSize * 0.05f).coerceAtLeast(2.dp) else (containerSize * 0.017f).coerceAtLeast(1.dp),
                    color = if (isSelected) WaterBlue else Color(0xFFE0E0E0),
                    shape = CircleShape
                )
                .background(
                    color = if (isSelected) backgroundColor.copy(alpha = 0.3f) else backgroundColor.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Use mood icons without tint to preserve their native designed colors
            when (mood.lowercase()) {
                "happy" -> Icon(
                    painter = painterResource(id = R.drawable.ic_happy),
                    contentDescription = "Happy",
                    tint = Color.Unspecified, // No tint - preserve native colors
                    modifier = Modifier.size(iconSize)
                )
                "sad" -> Icon(
                    painter = painterResource(id = R.drawable.ic_sad),
                    contentDescription = "Sad",
                    tint = Color.Unspecified, // No tint - preserve native colors
                    modifier = Modifier.size(iconSize)
                )
                "angry" -> Icon(
                    painter = painterResource(id = R.drawable.ic_angry),
                    contentDescription = "Angry",
                    tint = Color.Unspecified, // No tint - preserve native colors
                    modifier = Modifier.size(iconSize)
                )
                "peaceful" -> Icon(
                    painter = painterResource(id = R.drawable.ic_peaceful),
                    contentDescription = "Peaceful",
                    tint = Color.Unspecified, // No tint - preserve native colors
                    modifier = Modifier.size(iconSize)
                )
                "playful" -> Icon(
                    painter = painterResource(id = R.drawable.ic_playful),
                    contentDescription = "Playful",
                    tint = Color.Unspecified, // No tint - preserve native colors
                    modifier = Modifier.size(iconSize)
                )
                "scared" -> Icon(
                    painter = painterResource(id = R.drawable.ic_scared),
                    contentDescription = "Scared",
                    tint = Color.Unspecified, // No tint - preserve native colors
                    modifier = Modifier.size(iconSize)
                )
                "shy" -> Icon(
                    painter = painterResource(id = R.drawable.ic_shy),
                    contentDescription = "Shy",
                    tint = Color.Unspecified, // No tint - preserve native colors
                    modifier = Modifier.size(iconSize)
                )
                "surprised" -> Icon(
                    painter = painterResource(id = R.drawable.ic_surprised),
                    contentDescription = "Surprised",
                    tint = Color.Unspecified, // No tint - preserve native colors
                    modifier = Modifier.size(iconSize)
                )
                "frustrated" -> Icon(
                    painter = painterResource(id = R.drawable.ic_frustrated),
                    contentDescription = "Frustrated",
                    tint = Color.Unspecified, // No tint - preserve native colors
                    modifier = Modifier.size(iconSize)
                )
                else -> Text(
                    text = mood.take(2).uppercase(),
                    fontFamily = NotoSans,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) WaterBlue else MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Add mood label below the icon
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = mood.replaceFirstChar { it.uppercase() },
            fontFamily = NotoSans,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) WaterBlue else MaterialTheme.colorScheme.onSurface,
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
        shape = RoundedCornerShape(ScreenUtils.responsivePadding()),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) WaterBlue.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = if (isSelected)
            androidx.compose.foundation.BorderStroke(2.dp, WaterBlue)
        else
            getCardBorder()
    ) {
        Text(
            text = tag,
            fontFamily = NotoSans,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) WaterBlue else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = ScreenUtils.responsiveSpacing(), vertical = 6.dp)
        )
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
        ActivityType.CHECK_IN -> WaterBlue // Fallback (shouldn't be displayed)
    }

    val activityTypeText = when (activity.activityType) {
        ActivityType.REFLECT -> "Reflection"
        ActivityType.ASK -> "Chat"
        ActivityType.CHECK_IN -> "Check-in" // Fallback (shouldn't be displayed)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(ScreenUtils.responsiveSpacing()),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(ScreenUtils.responsivePadding())
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
                            .size(ScreenUtils.responsiveIconSize(8.dp))
                            .background(activityColor, CircleShape)
                    )

                    Spacer(modifier = Modifier.width(ScreenUtils.responsiveSpacing()))

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
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Preview content or mood info
            if (activity.preview != null) {
                Text(
                    text = activity.preview,
                    fontFamily = NotoSans,
                    fontSize = 14.sp,
                    color = Color(0xFF666666),
                    maxLines = 2,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Tags (only show if there are tags and it's not a mood check-in)
            if (activity.tags.isNotEmpty() && activity.activityType != ActivityType.CHECK_IN) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    activity.tags.take(3).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(ScreenUtils.responsiveSpacing())
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tag,
                                fontFamily = NotoSans,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        }
    }
}

@Composable
private fun EmptySearchResults() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Water drop icon
        Image(
            painter = painterResource(id = R.drawable.ic_orielle_drop),
            contentDescription = "Water Drop",
            modifier = Modifier.size(ScreenUtils.responsiveImageSize())
        )

        Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding()))

        Text(
            text = "No Memories Found",
            fontFamily = NotoSans,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
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
            modifier = Modifier.padding(ScreenUtils.responsivePadding())
        ) {
            SearchInput(
                query = "",
                onQueryChange = {}
            )

            Spacer(modifier = Modifier.height(ScreenUtils.responsiveIconSize()))

            FilterSection(
                selectedTypes = setOf(ActivityType.REFLECT),
                selectedMoods = setOf("happy"),
                selectedTags = setOf("growth"),
                availableMoods = listOf("happy", "sad", "angry", "peaceful", "playful", "scared", "shy", "surprised", "frustrated"),
                availableTags = listOf("growth", "mindfulness", "relationships", "work"),
                onTypeToggle = {},
                onMoodToggle = {},
                onTagToggle = {}
            )
        }
    }
}