package com.orielle.ui.screens.remember

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
            // Navigation bar with minimal styling
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (MaterialTheme.colorScheme.background == DarkGray) Color(0xFF1A1A1A) else Color.White)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DashboardNavItem(
                    icon = R.drawable.ic_orielle_drop,
                    label = "Home",
                    selected = false,
                    onClick = { navController.navigate("home_graph") }
                )
                DashboardNavItem(
                    icon = R.drawable.reflect,
                    label = "Reflect",
                    selected = false,
                    onClick = { navController.navigate("reflect") }
                )
                DashboardNavItem(
                    icon = R.drawable.ask,
                    label = "Ask",
                    selected = false,
                    onClick = { navController.navigate("ask") }
                )
                DashboardNavItem(
                    icon = R.drawable.remember,
                    label = "Remember",
                    selected = true,
                    onClick = { /* Already on remember */ }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Calendar Component
                CalendarComponent(
                    calendarDays = uiState.calendarDays,
                    currentMonthYear = uiState.currentMonthYear,
                    onNextMonth = { viewModel.nextMonth() },
                    onPreviousMonth = { viewModel.previousMonth() },
                    onDayClick = { viewModel.onDayClick(it) },
                    onMonthClick = { viewModel.showMonthSelector() },
                    onYearClick = { viewModel.showYearSelector() }
                )
            }

            // Daily Glimpse Panel - positioned at bottom
            if (uiState.showDayDetail && uiState.selectedDay != null) {
                DailyGlimpsePanel(
                    day = uiState.selectedDay!!,
                    onDismiss = { viewModel.hideDayDetail() },
                    onNavigateToDetail = { activity ->
                        when (activity.activityType) {
                            ActivityType.REFLECT -> navController.navigate("journal_detail/${activity.relatedId}")
                            ActivityType.ASK -> navController.navigate("conversation_detail/${activity.relatedId}")
                            ActivityType.CHECK_IN -> navController.navigate("mood_detail")
                        }
                        viewModel.hideDayDetail()
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
private fun CalendarComponent(
    calendarDays: List<CalendarDay>,
    currentMonthYear: String,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit,
    onDayClick: (CalendarDay) -> Unit,
    onMonthClick: () -> Unit,
    onYearClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(410.dp),
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
                onNextMonth = onNextMonth,
                onPreviousMonth = onPreviousMonth,
                onMonthClick = onMonthClick,
                onYearClick = onYearClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Day Headers
            DayHeaders()

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Grid
            CalendarGrid(
                calendarDays = calendarDays,
                onDayClick = onDayClick
            )
        }
    }
}

@Composable
private fun MonthYearHeader(
    monthYear: String,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit,
    onMonthClick: () -> Unit,
    onYearClick: () -> Unit
) {
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
                onClick = { onDayClick(day) }
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
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

            // Debug: Show activity count
            if (day.activities.isNotEmpty()) {
                Text(
                    text = "${day.activities.size}",
                    fontSize = 8.sp,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }

            // Activity indicators (dots) - positioned at bottom
            if (day.activities.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    // Reflection dot (teal)
                    if (day.hasReflectActivity) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(StillwaterTeal)
                        )
                    }

                    // Ask dot (gold)
                    if (day.hasAskActivity) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AuroraGold)
                        )
                    }

                    // Check-in dot (blue)
                    if (day.hasCheckInActivity) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
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
                .height(380.dp)
                .align(Alignment.BottomCenter),
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

@Composable
private fun DashboardNavItem(
    icon: Int,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (selected) WaterBlue else Charcoal,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
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