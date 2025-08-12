package com.orielle.ui.screens.remember

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.tooling.preview.Preview
import com.orielle.ui.theme.OrielleTheme
import androidx.navigation.NavController
import com.orielle.domain.model.ChatConversation
import com.orielle.domain.model.JournalEntry
import com.orielle.domain.model.MoodCheckIn
import com.orielle.ui.theme.*
import com.orielle.R
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import java.text.SimpleDateFormat
import java.util.*

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
                onSearchClick = { viewModel.showSearchDialog() }
            )
        },
        bottomBar = {
            // Bottom Navigation Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
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
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {

            // Journal Entry Cards - Show only 1 entry for now
            if (uiState.journalEntries.isNotEmpty()) {
                item(contentType = "journal_entry") {
                    JournalEntryCard(
                        entry = uiState.journalEntries.first(),
                        onClick = { navController.navigate("journal_detail/${uiState.journalEntries.first().id}") }
                    )
                }
            }

            // Conversation Cards
            items(
                items = uiState.conversations,
                key = { it.id },
                contentType = { "conversation" }
            ) { conversation ->
                ConversationCard(
                    conversation = conversation,
                    onClick = { navController.navigate("conversation_detail/${conversation.id}") }
                )
            }

            // Weekly Mood Horizon Card
            if (uiState.weeklyMoodData.isNotEmpty()) {
                item(contentType = "mood_horizon") {
                    WeeklyMoodHorizonCard(
                        moodData = uiState.weeklyMoodData,
                        onClick = { navController.navigate("mood_detail") }
                    )
                }
            }

            // Milestone Card
            if (uiState.milestoneMessage != null) {
                item(contentType = "milestone") {
                    MilestoneCard(message = uiState.milestoneMessage!!)
                }
            }
        }
    }

    // Search Dialog
    if (uiState.showSearchDialog) {
        SearchFilterDialog(
            onDismiss = { viewModel.hideSearchDialog() },
            onApplyFilters = { dateFilter, tagFilter ->
                viewModel.applyFilters(dateFilter, tagFilter)
                viewModel.hideSearchDialog()
            }
        )
    }
}

@Composable
private fun RememberHeader(
    onSearchClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Remember",
            style = Typography.headlineLarge.copy(
                color = Color(0xFF333333), // #333333 - Charcoal
                fontWeight = FontWeight.Bold
            )
        )

        IconButton(onClick = onSearchClick) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search and filter",
                tint = Color(0xFF333333), // #333333 - Charcoal
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun JournalEntryCard(
    entry: JournalEntry,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()) }

    // Use the same yellow background as the debug card
    val backgroundColor = Color(0xFFFFF3CD) // Same yellow as debug card

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Reflection from ${dateFormat.format(entry.timestamp)}",
                fontFamily = Typography.bodyMedium.fontFamily, // Noto Sans equivalent
                fontSize = 14.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Normal, // 400
                color = Color(0xFFB75100), // #B75100 - Orange-brown
                modifier = Modifier.padding(bottom = 5.dp)
            )

            Text(
                text = entry.content.take(120) + if (entry.content.length > 120) "..." else "",
                fontFamily = Typography.bodyMedium.fontFamily, // Noto Sans equivalent
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Normal, // 400
                color = Color(0xFF333333), // #333333 - Charcoal
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            // Show tags if they exist
            if (entry.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.wrapContentSize(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    entry.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFF856404), // Dark brown for tags
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 12.sp,
                                color = Color(0xFFFFF3CD), // Light yellow text on dark brown
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationCard(
    conversation: ChatConversation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE0E0E0) // #E0E0E0 - Light Gray
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "A thought from Orielle & You",
                fontFamily = Typography.bodyMedium.fontFamily, // Noto Sans equivalent
                fontSize = 14.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Normal, // 400
                color = Color(0xFF333333), // #333333 - Charcoal
                modifier = Modifier.padding(bottom = 5.dp)
            )

            Text(
                text = conversation.lastMessagePreview ?: "Continue your conversation...",
                fontFamily = Typography.bodyMedium.fontFamily, // Noto Sans equivalent
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Normal, // 400
                color = Color(0xFF333333), // #333333 - Charcoal
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun WeeklyMoodHorizonCard(
    moodData: List<MoodCheckIn>,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val calendar = Calendar.getInstance()

    // Get the date range for the week
    calendar.firstDayOfWeek = Calendar.MONDAY
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val startOfWeek = calendar.time
    calendar.add(Calendar.DAY_OF_YEAR, 6)
    val endOfWeek = calendar.time

    val weekRangeText = "${dateFormat.format(startOfWeek)} - ${dateFormat.format(endOfWeek)}, ${Calendar.getInstance().get(Calendar.YEAR)}"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFFFFF) // #FFFFFF - White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Your Weekly Horizon",
                fontFamily = Typography.titleLarge.fontFamily, // Noto Sans equivalent
                fontSize = 20.sp,
                lineHeight = 27.sp,
                fontWeight = FontWeight.Bold, // 700
                color = Color(0xFF333333) // #333333 - Charcoal
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = weekRangeText,
                fontFamily = Typography.bodyMedium.fontFamily, // Noto Sans equivalent
                fontSize = 14.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.Normal, // 400
                color = Color(0xFF333333) // #333333 - Charcoal
            )

            Spacer(modifier = Modifier.height(19.dp))

            // Mood Horizon Gradient - using the exact gradient from the CSS specs
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0x005DADE2), // rgba(93, 173, 226, 0) - 5.77%
                                Color(0x2B48C9B0), // rgba(72, 201, 176, 0.17) - 19.23%
                                Color(0x15F1C40F), // rgba(241, 196, 15, 0.085) - 31.73%
                                Color(0x41AF7AC5), // rgba(175, 122, 197, 0.255) - 53.37%
                                Color(0x57549BC7), // rgba(84, 153, 199, 0.34) - 76.44%
                                Color(0x6C566573), // rgba(86, 101, 115, 0.425) - 82.69%
                                Color(0x8034495E)  // rgba(52, 73, 94, 0.5) - 96.63%
                            ),
                            startX = 0f,
                            endX = 1f
                        )
                    )
            )
        }
    }
}

@Composable
private fun MilestoneCard(
    message: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF79B4B7) // #79B4B7 - Stillwater Teal
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Achievement icon - military tech/medal icon
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(
                        color = Color(0xFFE4C27A), // #E4C27A - Gold background
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Using a medal-like icon (you can replace with actual military_tech icon)
                Text(
                    text = "🏅", // Medal emoji as placeholder
                    fontSize = 16.sp,
                    color = Color(0xFFE4C27A)
                )
            }

            Spacer(modifier = Modifier.width(13.dp)) // 73px - 36px - 24px = 13px spacing

            // Text content - supporting two lines as shown in the image
            Text(
                text = message,
                fontFamily = Typography.bodyMedium.fontFamily, // Noto Sans equivalent
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Normal, // 400
                color = Color(0xFFFFFFFF), // #FFFFFF - White
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SearchFilterDialog(
    onDismiss: () -> Unit,
    onApplyFilters: (String?, String?) -> Unit
) {
    var selectedDateFilter by remember { mutableStateOf<String?>(null) }
    var selectedTagFilter by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Filter Feed") },
        text = {
            Column {
                Text("Date Filter")
                // Add date picker or date range selector here

                Spacer(modifier = Modifier.height(16.dp))

                Text("Tag Filter")
                // Add tag selector here
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onApplyFilters(selectedDateFilter, selectedTagFilter) }
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

// Helper functions
@Composable
private fun getMoodColor(mood: String?): Color? {
    return when (mood?.lowercase()) {
        "happy", "joyful" -> HappyYellow
        "calm", "flowing" -> CalmBlue
        "content", "peaceful" -> ContentTeal
        "anxious", "uncertain" -> AnxiousPurple
        "sad", "gloomy" -> SadGray
        "tired", "neutral" -> TiredNeutral
        else -> null
    }
}

@Composable
private fun getWeeklyMoodColors(moodData: List<MoodCheckIn>): List<Color> {
    val colors = mutableListOf<Color>()

    // Generate 7 colors for the week
    for (i in 0..6) {
        val moodForDay = moodData.getOrNull(i)
        val moodColor = getMoodColor(moodForDay?.mood)
        colors.add(moodColor ?: Color(0xFFE5E5E5)) // Use default light gray if no mood color
    }

    // If we don't have 7 moods, fill with default colors
    while (colors.size < 7) {
        colors.add(Color(0xFFE5E5E5)) // Light gray for missing data
    }

    return colors
}

@Composable
private fun DashboardNavItem(icon: Int, label: String, selected: Boolean, onClick: () -> Unit = {}) {
    val unselectedTextColor = Color(0xFF333333) // #333333 - Charcoal

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = label,
            modifier = Modifier.size(28.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = if (selected) StillwaterTeal else unselectedTextColor
            )
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .height(2.dp)
                    .width(32.dp)
                    .background(StillwaterTeal)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RememberScreenPreview() {
    OrielleTheme {
        // Preview placeholder - you can add sample data here
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Remember Screen Preview",
                style = Typography.headlineMedium,
                color = Color(0xFF333333)
            )
        }
    }
}
