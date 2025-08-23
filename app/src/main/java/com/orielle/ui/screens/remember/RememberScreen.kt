package com.orielle.ui.screens.remember

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
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
import com.orielle.ui.theme.OrielleTheme
import com.orielle.domain.model.CalendarDay
import com.orielle.domain.model.UserActivity
import com.orielle.domain.model.ActivityType
import com.orielle.ui.components.BottomNavigation
import com.orielle.R
import java.util.Date
import java.util.Calendar
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun RememberScreen(
    navController: NavController,
    viewModel: RememberViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRememberData()
    }

    Scaffold(
        topBar = {
            RememberHeader(
                onClearSearch = { viewModel.clearSearch() },
                onNavigateToSearch = { navController.navigate("remember_search") }
            )
        },
        bottomBar = {
            BottomNavigation(
                navController = navController,
                currentRoute = "remember"
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(SoftSand)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                // Calendar Section
                CalendarSection(
                    calendarDays = uiState.calendarDays,
                    currentMonthYear = uiState.currentMonthYear,
                    selectedDay = uiState.selectedDay,
                    showMonthSelector = uiState.showMonthSelector,
                    showYearSelector = uiState.showYearSelector,
                    onDayClick = { viewModel.onDayClick(it) },
                    onNextMonth = { viewModel.nextMonth() },
                    onPreviousMonth = { viewModel.previousMonth() },
                    onMonthClick = { viewModel.showMonthSelector() },
                    onYearClick = { viewModel.showYearSelector() },
                    onSelectMonth = { viewModel.selectMonth(it) },
                    onSelectYear = { viewModel.selectYear(it) },
                    onHideMonthSelector = { viewModel.hideMonthSelector() },
                    onHideYearSelector = { viewModel.hideYearSelector() }
                )

            }

            // Loading indicator
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = WaterBlue)
                }
            }

            // Error message
            if (uiState.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error!!,
                        color = Color.Red,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Daily Glimpse Panel - Display as overlay
            if (uiState.selectedDay != null && uiState.showDayDetail) {
                DailyGlimpsePanel(
                    day = uiState.selectedDay!!,
                    onDismiss = { viewModel.onDayDetailDismiss() },
                    onNavigateToDetail = { activity ->
                        when (activity.activityType) {
                            ActivityType.REFLECT -> navController.navigate("journal_detail/${activity.relatedId}")
                            ActivityType.ASK -> navController.navigate("conversation_detail/${activity.relatedId}")
                            ActivityType.CHECK_IN -> navController.navigate("mood_check_in")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RememberHeader(
    onClearSearch: () -> Unit,
    onNavigateToSearch: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SoftSand)
            .padding(20.dp)
    ) {
        // Title
        Text(
            text = "Remember",
            fontFamily = Lora,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Charcoal,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Search Bar
        SearchBar(
            query = searchQuery,
            onClearSearch = {
                searchQuery = ""
                onClearSearch()
            },
            onNavigateToSearch = onNavigateToSearch
        )
    }
}

@Composable
private fun SearchBar(
    query: String,
    onClearSearch: () -> Unit,
    onNavigateToSearch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToSearch() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Charcoal,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = if (query.isEmpty()) "Search by tag, mood, or memory..." else query,
                fontFamily = NotoSans,
                fontSize = 14.sp,
                color = if (query.isEmpty()) Color(0xFF999999) else Charcoal,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )

            if (query.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = {
                        onClearSearch()
                    }
                ) {
                    Text(
                        text = "Clear",
                        color = Charcoal,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarSection(
    calendarDays: List<CalendarDay>,
    currentMonthYear: String,
    selectedDay: CalendarDay?,
    showMonthSelector: Boolean,
    showYearSelector: Boolean,
    onDayClick: (CalendarDay) -> Unit,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit,
    onMonthClick: () -> Unit,
    onYearClick: () -> Unit,
    onSelectMonth: (Int) -> Unit,
    onSelectYear: (Int) -> Unit,
    onHideMonthSelector: () -> Unit,
    onHideYearSelector: () -> Unit
) {
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (showMonthSelector || showYearSelector) 610.dp else 410.dp), // Expand height when dropdowns are shown
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(19.dp)
            ) {
                // Month/Year Header with Navigation
                MonthYearHeader(
                    monthYear = currentMonthYear,
                    showMonthSelector = showMonthSelector,
                    showYearSelector = showYearSelector,
                    onNextMonth = onNextMonth,
                    onPreviousMonth = onPreviousMonth,
                    onMonthClick = onMonthClick,
                    onYearClick = onYearClick,
                    onSelectMonth = onSelectMonth,
                    onSelectYear = onSelectYear,
                    onHideMonthSelector = onHideMonthSelector,
                    onHideYearSelector = onHideYearSelector
                )

                if (!showMonthSelector && !showYearSelector) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Day Headers
                    DayHeaders()

                    Spacer(modifier = Modifier.height(8.dp))

                    // Calendar Grid
                    CalendarGrid(
                        calendarDays = calendarDays,
                        selectedDay = selectedDay,
                        onDayClick = onDayClick
                    )
                }
            }
        }

        // Invisible overlay to close dropdowns when clicking outside
        if (showMonthSelector || showYearSelector) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        onHideMonthSelector()
                        onHideYearSelector()
                    }
            ) {
                // Position the dropdowns above the overlay so they're clickable
                if (showMonthSelector) {
                    val currentMonth = currentMonthYear.split(" ")[0]
                    val monthIndex = listOf("January", "February", "March", "April", "May", "June",
                        "July", "August", "September", "October", "November", "December").indexOf(currentMonth)

                    MonthSelector(
                        currentSelectedMonth = if (monthIndex >= 0) monthIndex else Calendar.getInstance().get(Calendar.MONTH),
                        onSelectMonth = onSelectMonth,
                        onDismiss = onHideMonthSelector
                    )
                }

                if (showYearSelector) {
                    val currentYear = currentMonthYear.split(" ").getOrNull(1)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)

                    YearSelector(
                        currentSelectedYear = currentYear,
                        onSelectYear = onSelectYear,
                        onDismiss = onHideYearSelector
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthYearHeader(
    monthYear: String,
    showMonthSelector: Boolean,
    showYearSelector: Boolean,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit,
    onMonthClick: () -> Unit,
    onYearClick: () -> Unit,
    onSelectMonth: (Int) -> Unit,
    onSelectYear: (Int) -> Unit,
    onHideMonthSelector: () -> Unit,
    onHideYearSelector: () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Previous month arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous month",
                tint = Charcoal,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onPreviousMonth() }
            )

            // Month dropdown
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val parts = monthYear.split(" ")
                Text(
                    text = if (parts.isNotEmpty()) parts[0] else "Loading...", // Month name
                    fontFamily = NotoSans,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    modifier = Modifier.clickable { onMonthClick() }
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Month dropdown",
                    tint = Charcoal,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onMonthClick() }
                )
            }

            // Year dropdown
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val parts = monthYear.split(" ")
                Text(
                    text = if (parts.size >= 2) parts[1] else "2024", // Year
                    fontFamily = NotoSans,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Charcoal,
                    modifier = Modifier.clickable { onYearClick() }
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Year dropdown",
                    tint = Charcoal,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onYearClick() }
                )
            }

            // Next month arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next month",
                tint = Charcoal,
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onNextMonth() }
            )
        }


    }
}

@Composable
private fun MonthSelector(
    currentSelectedMonth: Int,
    onSelectMonth: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val months = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        LazyColumn(
            modifier = Modifier.heightIn(max = 200.dp),
            state = rememberLazyListState(initialFirstVisibleItemIndex = maxOf(0, currentSelectedMonth - 2))
        ) {
            items(months.size) { index ->
                val month = months[index]
                val isSelected = index == currentSelectedMonth

                Text(
                    text = month,
                    fontFamily = NotoSans,
                    fontSize = 16.sp,
                    color = if (isSelected) WaterBlue else Charcoal,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) WaterBlue.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable {
                            onSelectMonth(index)
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
                if (index < months.size - 1) {
                    HorizontalDivider(
                        color = Color(0xFFE0E0E0),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun YearSelector(
    currentSelectedYear: Int,
    onSelectYear: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)
    val years = (currentYear - 5..currentYear + 5).toList() // 5 years back, 5 years forward
    val selectedIndex = years.indexOf(currentSelectedYear).takeIf { it >= 0 } ?: years.indexOf(currentYear)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        LazyColumn(
            modifier = Modifier.heightIn(max = 200.dp),
            state = rememberLazyListState(initialFirstVisibleItemIndex = maxOf(0, selectedIndex - 2))
        ) {
            items(years.size) { index ->
                val year = years[index]
                val isSelected = year == currentSelectedYear

                Text(
                    text = year.toString(),
                    fontFamily = NotoSans,
                    fontSize = 16.sp,
                    color = if (isSelected) WaterBlue else Charcoal,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (isSelected) WaterBlue.copy(alpha = 0.1f) else Color.Transparent)
                        .clickable {
                            onSelectYear(year)
                            onDismiss()
                        }
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                )
                if (index < years.size - 1) {
                    HorizontalDivider(
                        color = Color(0xFFE0E0E0),
                        thickness = 0.5.dp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DayHeaders() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        dayNames.forEach { day ->
            Text(
                text = day,
                fontFamily = NotoSans,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Charcoal,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    calendarDays: List<CalendarDay>,
    selectedDay: CalendarDay?,
    onDayClick: (CalendarDay) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        horizontalArrangement = Arrangement.spacedBy(0.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        items(calendarDays) { day ->
            CalendarDayCell(
                day = day,
                isSelected = selectedDay?.let { it.dayOfMonth == day.dayOfMonth && it.date.time == day.date.time } ?: false,
                onClick = { onDayClick(day) }
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    if (day.dayOfMonth == 0) {
        // Empty cell for padding
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .clickable { onClick() }
                .background(
                    when {
                        day.isToday -> WaterBlue
                        isSelected -> WaterBlue.copy(alpha = 0.3f)
                        else -> Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            // Day number
            Text(
                text = day.dayOfMonth.toString(),
                fontFamily = NotoSans,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = if (day.isToday) Color.White else Charcoal
            )

            // Activity indicators (dots) - positioned at bottom
            if (day.hasReflectActivity || day.hasAskActivity || day.hasCheckInActivity) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Reflection dot (teal)
                    if (day.hasReflectActivity) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(StillwaterTeal)
                        )
                    }

                    // Ask dot (gold)
                    if (day.hasAskActivity) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(AuroraGold)
                        )
                    }

                    // Check-in dot (blue/water blue)
                    if (day.hasCheckInActivity) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(WaterBlue)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyGlimpsePanel(
    day: CalendarDay,
    onDismiss: () -> Unit,
    onNavigateToDetail: (UserActivity) -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == DarkGray

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp) // Increased height to cover more of the screen
                .align(Alignment.BottomCenter)
                .offset(y = 80.dp), // Offset to overhang the bottom navigation
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF2A2A2A) else Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp)
            ) {
                // Grabber
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE0E0E0))
                    )
                }

                // Header with date and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = formatDateForGlimpse(day.date),
                        fontFamily = NotoSans,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) SoftSand else Charcoal
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (isDark) SoftSand else Charcoal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Activities list - Group by type to avoid duplication
                if (day.activities.isNotEmpty()) {
                    val groupedActivities = day.activities.groupBy { it.activityType }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Show first Reflection activity only
                        groupedActivities[ActivityType.REFLECT]?.firstOrNull()?.let { activity ->
                            GlimpseActivityItem(
                                activity = activity,
                                onClick = { onNavigateToDetail(activity) }
                            )
                        }

                        // Show first Chat/Ask activity only
                        groupedActivities[ActivityType.ASK]?.firstOrNull()?.let { activity ->
                            GlimpseActivityItem(
                                activity = activity,
                                onClick = { onNavigateToDetail(activity) }
                            )
                        }

                        // Show first Mood Check-in activity only
                        groupedActivities[ActivityType.CHECK_IN]?.firstOrNull()?.let { activity ->
                            GlimpseActivityItem(
                                activity = activity,
                                onClick = { onNavigateToDetail(activity) }
                            )
                        }
                    }
                } else {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No activities on this day",
                            fontFamily = NotoSans,
                            fontSize = 16.sp,
                            color = if (isDark) Color(0xFFAAAAAA) else Color(0xFF999999),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GlimpseActivityItem(
    activity: UserActivity,
    onClick: () -> Unit
) {
    val isDark = MaterialTheme.colorScheme.background == DarkGray

    val activityColor = when (activity.activityType) {
        ActivityType.REFLECT -> StillwaterTeal
        ActivityType.ASK -> AuroraGold
        ActivityType.CHECK_IN -> WaterBlue
    }

    val activityTitle = when (activity.activityType) {
        ActivityType.REFLECT -> "Your Reflection"
        ActivityType.ASK -> "Chat with Orielle"
        ActivityType.CHECK_IN -> "Mood Check-in"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        // Title with color-coded type
        Text(
            text = activityTitle,
            fontFamily = NotoSans,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = activityColor
        )

        // Preview content
        if (activity.preview != null) {
            Text(
                text = activity.preview,
                fontFamily = NotoSans,
                fontSize = 16.sp,
                color = if (isDark) SoftSand else Charcoal,
                maxLines = 3,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Action link
        val actionText = when (activity.activityType) {
            ActivityType.REFLECT -> "Read more..."
            ActivityType.ASK -> "View conversation..."
            ActivityType.CHECK_IN -> "View details..."
        }

        Text(
            text = actionText,
            fontFamily = NotoSans,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = WaterBlue,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

// Helper function to format date for glimpse panel
private fun formatDateForGlimpse(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date
    val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val year = calendar.get(Calendar.YEAR)
    return "$month $day, $year"
}



// Preview function
@Preview(showBackground = true)
@Composable
fun RememberScreenPreview() {
    OrielleTheme {
        RememberScreen(
            navController = androidx.navigation.compose.rememberNavController()
        )
    }
}
