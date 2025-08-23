package com.orielle.ui.screens.remember

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
    themeManager: com.orielle.ui.theme.ThemeManager,
    viewModel: RememberViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = !MaterialTheme.colorScheme.background.equals(SoftSand)

    val backgroundColor = if (isDark) DarkGray else SoftSand
    val textColor = if (isDark) SoftSand else Charcoal
    val cardColor = if (isDark) Color(0xFF2A2A2A) else Color.White

    LaunchedEffect(Unit) {
        viewModel.loadRememberData()
    }

    Scaffold(
        topBar = {
            RememberHeader(
                backgroundColor = backgroundColor,
                textColor = textColor,
                onClearSearch = { viewModel.clearSearch() },
                onNavigateToSearch = { navController.navigate("remember_search") }
            )
        },
        bottomBar = {
            BottomNavigation(
                navController = navController,
                themeManager = themeManager
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(backgroundColor)
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
                    cardColor = cardColor,
                    textColor = textColor,
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

            // Skeleton loading for better UX
            if (uiState.isLoading) {
                SkeletonLoading()
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
                    cardColor = cardColor,
                    textColor = textColor,
                    isDark = isDark,
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
    backgroundColor: Color,
    textColor: Color,
    onClearSearch: () -> Unit,
    onNavigateToSearch: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(20.dp)
    ) {
        // Title
        Text(
            text = "Remember",
            fontFamily = Lora,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
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
    val isDark = !MaterialTheme.colorScheme.background.equals(SoftSand)
    val cardColor = if (isDark) Color(0xFF2A2A2A) else Color.White
    val textColor = if (isDark) SoftSand else Charcoal

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onNavigateToSearch() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
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
                tint = textColor,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = if (query.isEmpty()) "Search by tag, mood, or memory..." else query,
                fontFamily = NotoSans,
                fontSize = 14.sp,
                color = if (query.isEmpty()) Color(0xFF999999) else textColor,
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
                        color = textColor,
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
    cardColor: Color,
    textColor: Color,
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
                .wrapContentHeight(), // Let content determine height
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(19.dp)
            ) {
                // Month/Year Header with Navigation
                MonthYearHeader(
                    monthYear = currentMonthYear,
                    textColor = textColor,
                    onNextMonth = onNextMonth,
                    onPreviousMonth = onPreviousMonth,
                    onMonthClick = onMonthClick,
                    onYearClick = onYearClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Day Headers
                DayHeaders(textColor = textColor)

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar Grid - Always visible
                CalendarGrid(
                    calendarDays = calendarDays,
                    selectedDay = selectedDay,
                    textColor = textColor,
                    onDayClick = onDayClick
                )

                // Legend for activity dots - Ensure it's always visible
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ActivityLegend(textColor = textColor)
                }
            }
        }

        // Dropdown overlays - positioned above the calendar card
        if (showMonthSelector || showYearSelector) {
            // Invisible overlay to close dropdowns when clicking outside
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable {
                        onHideMonthSelector()
                        onHideYearSelector()
                    }
            ) {
                // Position the dropdowns above the calendar card
                if (showMonthSelector) {
                    val currentMonth = currentMonthYear.split(" ")[0]
                    val monthIndex = listOf("January", "February", "March", "April", "May", "June",
                        "July", "August", "September", "October", "November", "December").indexOf(currentMonth)

                    MonthSelector(
                        currentSelectedMonth = if (monthIndex >= 0) monthIndex else Calendar.getInstance().get(Calendar.MONTH),
                        cardColor = cardColor,
                        textColor = textColor,
                        onSelectMonth = onSelectMonth,
                        onDismiss = onHideMonthSelector
                    )
                }

                if (showYearSelector) {
                    val currentYear = currentMonthYear.split(" ").getOrNull(1)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)

                    YearSelector(
                        currentSelectedYear = currentYear,
                        cardColor = cardColor,
                        textColor = textColor,
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
    textColor: Color,
    onNextMonth: () -> Unit,
    onPreviousMonth: () -> Unit,
    onMonthClick: () -> Unit,
    onYearClick: () -> Unit
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
                tint = textColor,
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
                    color = textColor,
                    modifier = Modifier.clickable { onMonthClick() }
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Month dropdown",
                    tint = textColor,
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
                    color = textColor,
                    modifier = Modifier.clickable { onYearClick() }
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Year dropdown",
                    tint = textColor,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onYearClick() }
                )
            }

            // Next month arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next month",
                tint = textColor,
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
    cardColor: Color,
    textColor: Color,
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
        colors = CardDefaults.cardColors(containerColor = cardColor),
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
                    color = if (isSelected) WaterBlue else textColor,
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
    cardColor: Color,
    textColor: Color,
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
        colors = CardDefaults.cardColors(containerColor = cardColor),
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
                    color = if (isSelected) WaterBlue else textColor,
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
private fun DayHeaders(textColor: Color) {
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
                color = textColor,
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
    textColor: Color,
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
                textColor = textColor,
                onClick = { onDayClick(day) }
            )
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    isSelected: Boolean,
    textColor: Color,
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
                        day.isToday -> WaterBlue.copy(alpha = 0.7f) // Lighter highlight for current day
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
                color = if (day.isToday) Color.White else textColor
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
private fun ActivityLegend(textColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, textColor.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Activity Legend",
                fontFamily = NotoSans,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Reflection dot
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(StillwaterTeal)
                    )
                    Text(
                        text = "Reflection",
                        fontFamily = NotoSans,
                        fontSize = 12.sp,
                        color = textColor
                    )
                }

                // Ask dot
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AuroraGold)
                    )
                    Text(
                        text = "Chat/Ask",
                        fontFamily = NotoSans,
                        fontSize = 12.sp,
                        color = textColor
                    )
                }

                // Check-in dot
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(WaterBlue)
                    )
                    Text(
                        text = "Mood",
                        fontFamily = NotoSans,
                        fontSize = 12.sp,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyGlimpsePanel(
    day: CalendarDay,
    cardColor: Color,
    textColor: Color,
    isDark: Boolean,
    onDismiss: () -> Unit,
    onNavigateToDetail: (UserActivity) -> Unit
) {

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
                containerColor = cardColor
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
                        color = textColor
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = textColor,
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
private fun SkeletonLoading() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        // Header skeleton
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Title skeleton
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .height(32.dp)
                    .background(
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(8.dp)
                    )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Search bar skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        color = Color(0xFFE0E0E0),
                        shape = RoundedCornerShape(20.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Calendar skeleton
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Month/Year header skeleton
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(24.dp)
                            .background(
                                color = Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        repeat(2) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = Color(0xFFE0E0E0),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Calendar grid skeleton
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(42) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = Color(0xFFE0E0E0),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                    }
                }
            }
        }
    }
}



// Preview function
@Preview(showBackground = true)
@Composable
fun RememberScreenPreview() {
    val fakeThemeManager = com.orielle.ui.theme.ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    OrielleTheme {
        RememberScreen(
            navController = androidx.navigation.compose.rememberNavController(),
            themeManager = fakeThemeManager
        )
    }
}
