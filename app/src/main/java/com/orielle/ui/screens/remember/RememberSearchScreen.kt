package com.orielle.ui.screens.remember

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontStyle
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
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateRangeSelector(
    startDate: Date?,
    endDate: Date?,
    onStartDateChange: (Date?) -> Unit,
    onEndDateChange: (Date?) -> Unit,
    onClearDates: () -> Unit,
    textColor: Color,
    cardColor: Color
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val showStartDatePicker = remember { mutableStateOf(false) }
    val showEndDatePicker = remember { mutableStateOf(false) }

    // Date picker dialogs
    if (showStartDatePicker.value) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate?.time
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onStartDateChange(Date(millis))
                        }
                        showStartDatePicker.value = false
                    }
                ) {
                    Text("OK", color = WaterBlue)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showStartDatePicker.value = false }
                ) {
                    Text("Cancel", color = textColor.copy(alpha = 0.6f))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker.value) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate?.time
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker.value = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onEndDateChange(Date(millis))
                        }
                        showEndDatePicker.value = false
                    }
                ) {
                    Text("OK", color = WaterBlue)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showEndDatePicker.value = false }
                ) {
                    Text("Cancel", color = textColor.copy(alpha = 0.6f))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Compact horizontal layout
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = WaterBlue.copy(alpha = 0.08f),
                shape = RoundedCornerShape(ScreenUtils.responsiveSpacing())
            )
            .padding(ScreenUtils.responsivePadding()),
        horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date range icon
        Icon(
            imageVector = Icons.Default.DateRange,
            contentDescription = "Date Range",
            tint = WaterBlue,
            modifier = Modifier.size(ScreenUtils.responsiveIconSize(18.dp))
        )

        // Start date button
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { showStartDatePicker.value = true },
            shape = RoundedCornerShape(ScreenUtils.responsiveSpacing()),
            colors = CardDefaults.cardColors(
                containerColor = if (startDate != null) WaterBlue.copy(alpha = 0.1f) else Color.Transparent
            ),
            border = BorderStroke(
                1.dp,
                if (startDate != null) WaterBlue else textColor.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenUtils.responsiveSpacing(), vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Start Date",
                    tint = if (startDate != null) WaterBlue else textColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = startDate?.let { dateFormat.format(it) } ?: "From",
                    fontFamily = NotoSans,
                    fontSize = 13.sp,
                    fontWeight = if (startDate != null) FontWeight.Medium else FontWeight.Normal,
                    color = if (startDate != null) WaterBlue else textColor.copy(alpha = 0.6f)
                )
            }
        }

        // Arrow separator
        Text(
            text = "→",
            fontSize = 16.sp,
            color = textColor.copy(alpha = 0.4f)
        )

        // End date button
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { showEndDatePicker.value = true },
            shape = RoundedCornerShape(ScreenUtils.responsiveSpacing()),
            colors = CardDefaults.cardColors(
                containerColor = if (endDate != null) WaterBlue.copy(alpha = 0.1f) else Color.Transparent
            ),
            border = BorderStroke(
                1.dp,
                if (endDate != null) WaterBlue else textColor.copy(alpha = 0.3f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenUtils.responsiveSpacing(), vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "End Date",
                    tint = if (endDate != null) WaterBlue else textColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = endDate?.let { dateFormat.format(it) } ?: "To",
                    fontFamily = NotoSans,
                    fontSize = 13.sp,
                    fontWeight = if (endDate != null) FontWeight.Medium else FontWeight.Normal,
                    color = if (endDate != null) WaterBlue else textColor.copy(alpha = 0.6f)
                )
            }
        }

        // Clear button (only show if dates are selected)
        if (startDate != null || endDate != null) {
            IconButton(
                onClick = onClearDates,
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        color = WaterBlue.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Clear dates",
                    tint = WaterBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

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
                contentPadding = PaddingValues(bottom = ScreenUtils.responsiveIconSize()),
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
                    Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing()))
                }

                item {
                    // Date Range Selector
                    DateRangeSelector(
                        startDate = uiState.startDate,
                        endDate = uiState.endDate,
                        onStartDateChange = { viewModel.updateStartDate(it) },
                        onEndDateChange = { viewModel.updateEndDate(it) },
                        onClearDates = { viewModel.clearDateRange() },
                        textColor = MaterialTheme.colorScheme.onBackground,
                        cardColor = MaterialTheme.colorScheme.surface
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

@Composable
private fun SearchTopBar(
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (ScreenUtils.isSmallScreen()) 16.dp else 24.dp,
                end = if (ScreenUtils.isSmallScreen()) 16.dp else 24.dp,
                top = if (ScreenUtils.isSmallScreen()) 6.dp else 8.dp,
                bottom = if (ScreenUtils.isSmallScreen()) 6.dp else 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = "Filter & Search",
            fontFamily = Lora,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Empty space to center the title
        Spacer(modifier = Modifier.width(48.dp))
    }
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
                    // Create a 3x3 grid layout for mood icons - similar to mood check-in but smaller
                    val moodChunks = availableMoods.chunked(3)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding() * 0.75f)
                    ) {
                        moodChunks.forEach { moodRow ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding() * 0.75f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                moodRow.forEach { mood ->
                                    MoodFilterChip(
                                        mood = mood,
                                        isSelected = selectedMoods.contains(mood),
                                        onClick = { onMoodToggle(mood) }
                                    )
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
        FilterGroup(
            title = "Tags",
            content = {
                if (availableTags.isNotEmpty()) {
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
                } else {
                    Text(
                        text = "No tags available",
                        fontFamily = NotoSans,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        )
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
    // Use larger sizes to take up more of the row space
    val containerSize = (80 * ScreenUtils.getTextScaleFactor()).dp
    val iconSize = (48 * ScreenUtils.getTextScaleFactor()).dp

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

    Box(
        modifier = Modifier
            .clickable { onClick() }
            .padding(ScreenUtils.responsiveTextSpacing()),
        contentAlignment = Alignment.Center
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

        // No text below the icon - show only the icon
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

            // Metadata section for REFLECT activities
            if (activity.activityType == ActivityType.REFLECT) {
                Column(
                    modifier = Modifier.padding(top = ScreenUtils.responsivePadding()),
                    verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing())
                ) {
                    // Prompt (if available)
                    if (activity.promptText != null && activity.promptText.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing())
                        ) {
                            Text(
                                text = "💭",
                                fontSize = (12 * ScreenUtils.getTextScaleFactor()).sp
                            )
                            Text(
                                text = activity.promptText?.replace("+", " ") ?: "",
                                fontFamily = NotoSans,
                                fontSize = (12 * ScreenUtils.getTextScaleFactor()).sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Location (if available)
                    if (activity.location != null && activity.location.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing())
                        ) {
                            Text(
                                text = "📍",
                                fontSize = (12 * ScreenUtils.getTextScaleFactor()).sp
                            )
                            Text(
                                text = activity.location,
                                fontFamily = NotoSans,
                                fontSize = (12 * ScreenUtils.getTextScaleFactor()).sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Mood (if available) - show only icon, not text
                    // Note: Disabled for reflection results as mood should only show for check-ins
                    // if (activity.mood != null && activity.mood.isNotEmpty()) {
                    //     Row(
                    //         verticalAlignment = Alignment.CenterVertically,
                    //         horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing())
                    //     ) {
                    //         // Mood icon based on mood type
                    //         val moodIcon = when (activity.mood.lowercase()) {
                    //             "happy" -> "☀️"
                    //             "sad" -> "🌧️"
                    //             "angry" -> "⛈️"
                    //             "peaceful" -> "🌙"
                    //             "playful" -> "⭐"
                    //             "scared" -> "👻"
                    //             "shy" -> "😊"
                    //             "surprised" -> "❗"
                    //             "frustrated" -> "⚡"
                    //             else -> "🌤️"
                    //         }
                    //         Text(
                    //             text = moodIcon,
                    //             fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp
                    //         )
                    //     }
                    // }

                    // Tags (if available)
                    if (activity.tags.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing())
                        ) {
                            activity.tags.take(3).forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .background(
                                            color = StillwaterTeal.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(ScreenUtils.responsiveSpacing())
                                        )
                                        .padding(
                                            horizontal = ScreenUtils.responsivePadding(),
                                            vertical = ScreenUtils.responsiveTextSpacing()
                                        )
                                ) {
                                    Text(
                                        text = tag,
                                        fontFamily = NotoSans,
                                        fontSize = (10 * ScreenUtils.getTextScaleFactor()).sp,
                                        color = StillwaterTeal,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (activity.tags.size > 3) {
                                Text(
                                    text = "+${activity.tags.size - 3}",
                                    fontFamily = NotoSans,
                                    fontSize = (10 * ScreenUtils.getTextScaleFactor()).sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(start = ScreenUtils.responsiveTextSpacing())
                                )
                            }
                        }
                    }
                }
            } else {
                // Tags for non-REFLECT activities (existing logic)
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
