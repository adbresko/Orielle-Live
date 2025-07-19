package com.orielle.ui.screens.home

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.orielle.R
import com.orielle.ui.theme.OrielleTheme
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.BorderStroke
import org.threeten.bp.LocalDate
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.launch
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import kotlinx.coroutines.launch
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Logout
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    navController: NavController? = null // Allow navigation if provided
) {
    val uiState by viewModel.uiState.collectAsState()
    val dashboardState by viewModel.dashboardState.collectAsState()
    val userName = uiState.userName ?: "User"
    val isPremium = uiState.isPremium ?: false
    val today = remember { LocalDate.now() }
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    val datesOfWeek = (0..6).map { today.minusDays((today.dayOfWeek.value % 7 - it).toLong()) }
    val selectedDayIndex = today.dayOfWeek.value % 7
    var selectedMood by remember { mutableStateOf<String?>(null) }
    // Remove snackbar for now
    // val snackbarHostState = rememberSnackbarHostState()
    val coroutineScope = rememberCoroutineScope()
    val bg = MaterialTheme.colorScheme.background
    val context = LocalContext.current
    // Listen for log out event
    val logOutEvent = viewModel.logOutEvent
    val currentNavController = navController
    LaunchedEffect(logOutEvent) {
        logOutEvent.collect {
            // Navigate to auth/login screen (replace with your actual route)
            currentNavController?.navigate("auth_graph") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = {
            HomeHeader(
                userName = userName,
                isPremium = isPremium,
                onLogOutClick = { viewModel.logOut() }
            )
        },
        bottomBar = {
            HomeBottomBarWithFAB()
        },
        containerColor = Color.Transparent
        // Remove snackbar host for now
        // snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (dashboardState) {
            is DashboardState.Initial -> {
                // Minimalist: Only show check-in card
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bg)
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(32.dp))
                    Text(
                        text = "How is your inner weather today?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    MoodCheckInRow(
                        selectedMood = selectedMood,
                        onMoodSelected = { mood ->
                            selectedMood = mood
                            // Navigate to mood check-in flow
                            currentNavController?.navigate("mood_check_in")
                        },
                        onMoodLongPress = { /* Optionally handle long press */ }
                    )
                    Spacer(Modifier.height(32.dp))
                    Button(
                        onClick = {
                            // Navigate to mood check-in flow
                            currentNavController?.navigate("mood_check_in")
                        },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        Text("Check In")
                    }
                    TextButton(onClick = {
                        // User skips check-in
                        viewModel.onCheckInCompletedOrSkipped()
                    }) {
                        Text("Skip for now")
                    }
                }
            }
            is DashboardState.Unfolded -> {
                // Full dashboard as before
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(bg)
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(8.dp))
                    CalendarRow(daysOfWeek, datesOfWeek, selectedDayIndex)
                    Spacer(Modifier.height(18.dp))
                    Text(
                        text = "good morning.",
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    ReflectionCards()
                    Spacer(Modifier.height(24.dp))
                    MoodCheckInRow(
                        selectedMood = selectedMood,
                        onMoodSelected = { mood ->
                            selectedMood = mood
                            // Optionally allow mood check-in again
                        },
                        onMoodLongPress = { /* Optionally handle long press */ }
                    )
                    Spacer(Modifier.height(32.dp))
                    Spacer(Modifier.height(90.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(userName: String, isPremium: Boolean, onLogOutClick: () -> Unit) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_orielle_drop),
            contentDescription = "Orielle Icon",
            modifier = Modifier.size(28.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.weight(1f))
        // Profile icon (right)
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            modifier = Modifier.size(36.dp)
        ) {
            if (isPremium) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Premium User",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(7.dp).size(22.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Guest User",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(7.dp).size(22.dp)
                )
            }
        }
        // Overflow menu for log out
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Log Out") },
                    onClick = {
                        menuExpanded = false
                        onLogOutClick()
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Log Out")
                    }
                )
            }
        }
    }
}

@Composable
private fun CalendarRow(days: List<String>, dates: List<LocalDate>, selectedIndex: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        days.forEachIndexed { idx, day ->
            val isSelected = idx == selectedIndex
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent
                ) {
                    Text(
                        text = dates[idx].dayOfMonth.toString().padStart(2, '0'),
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReflectionCards() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onBackground),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Let’s start your day",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.background
                )
                Text(
                    text = "with morning preparation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_happy), // Placeholder, use your own
                    contentDescription = "Morning Icon",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.background
                )
            }
        }
        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Evening Reflection",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Sum up your day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_playful), // Placeholder, use your own
                    contentDescription = "Evening Icon",
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun MoodCheckInRow(
    selectedMood: String?,
    onMoodSelected: (String) -> Unit,
    onMoodLongPress: (String) -> Unit
) {
    val moods = listOf(
        Triple("Angry", R.drawable.ic_angry, Color(0xFFE57373)),
        Triple("Sad", R.drawable.ic_sad, Color(0xFFFFB74D)),
        Triple("Okay", R.drawable.ic_peaceful, Color(0xFF64B5F6)),
        Triple("Good", R.drawable.ic_playful, Color(0xFF81C784)),
        Triple("Great", R.drawable.ic_surprised, Color(0xFFBA68C8))
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        moods.forEach { (label, icon, color) ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(4.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (selectedMood == label) color.copy(alpha = 0.25f) else color.copy(alpha = 0.12f),
                    modifier = Modifier
                        .size(54.dp)
                        .clickable(
                            onClick = { onMoodSelected(label) },
                            indication = rememberRipple(bounded = true),
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                    tonalElevation = 0.dp
                ) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = label,
                        modifier = Modifier.padding(13.dp).size(28.dp),
                        tint = color
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selectedMood == label) color else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun OrielleWatermark() {
    val infiniteTransition = rememberInfiniteTransition(label = "watermarkPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_orielle_drop),
            contentDescription = "Orielle Watermark",
            modifier = Modifier
                .size(320.dp)
                .graphicsLayer {
                    alpha = 0.10f
                    scaleX = scale
                    scaleY = scale
                },
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun TopBarWithCenteredLogo(userName: String, isPremium: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(WindowInsets.statusBars.asPaddingValues())
            .padding(vertical = 12.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: User name
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1
            )
        }
        // Center: Orielle drop logo
        Box(
            modifier = Modifier.width(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_orielle_drop),
                contentDescription = "Orielle Logo",
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        // Right: Profile status
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                if (isPremium) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Premium User",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Premium",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Guest User",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Guest",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomeBanner(gradientColors: List<Color>, message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                brush = Brush.horizontalGradient(gradientColors)
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 18.dp, end = 18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.WbSunny,
                contentDescription = "Sun Icon",
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = message,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                lineHeight = 26.sp
            )
        }
    }
}

@Composable
private fun MoodTrackerSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "How do you feel today?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MoodIconCircle(icon = R.drawable.ic_happy, color = Color(0xFFE57373), label = "Angry")
            MoodIconCircle(icon = R.drawable.ic_happy, color = Color(0xFFFFB74D), label = "Sad")
            MoodIconCircle(icon = R.drawable.ic_happy, color = Color(0xFF64B5F6), label = "Okay")
            MoodIconCircle(icon = R.drawable.ic_happy, color = Color(0xFF81C784), label = "Good")
            MoodIconCircle(icon = R.drawable.ic_happy, color = Color(0xFFBA68C8), label = "Great")
        }
    }
}

@Composable
private fun MoodIconCircle(icon: Int, color: Color, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.18f),
            modifier = Modifier
                .size(54.dp)
                .clickable(
                    onClick = { /* TODO: handle click if needed */ },
                    indication = rememberRipple(bounded = true),
                    interactionSource = remember { MutableInteractionSource() }
                ),
            tonalElevation = 0.dp
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = label,
                modifier = Modifier.padding(13.dp).size(28.dp),
                tint = color
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ActionButtonsSection() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Correct usage: apply Modifier.weight(1f) to the Row children, not to Card itself
        ActionButton(
            icon = Icons.Default.Chat,
            label = "Chat with Orielle",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        ActionButton(
            icon = Icons.Default.AutoAwesome,
            label = "Reflect with Orielle",
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionButton(icon: ImageVector, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier, // Accept modifier from parent
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.13f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, color = color, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun RecentCheckInsSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Your Recent Check-Ins",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        val checkIns = listOf(
            "😊 Good mood - 9:00 AM",
            "😐 Okay mood - Yesterday",
            "😢 Sad mood - 2 days ago"
        )
        checkIns.forEach { entry ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = entry,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
private fun HomeBottomBarWithFAB() {
    val hasCheckedInToday = false // Replace with real logic
    val selectedIndex = 0 // Replace with real navigation state
    val items = listOf(
        BottomBarItem(
            label = "Check-In",
            icon = Icons.Default.Edit,
            enabled = !hasCheckedInToday
        ),
        BottomBarItem(
            label = "Ask",
            icon = Icons.Default.QuestionAnswer
        ),
        BottomBarItem(
            label = "Reflect",
            icon = Icons.Default.Lightbulb
        ),
        BottomBarItem(
            label = "Remember",
            icon = Icons.Default.Bookmark
        )
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomBarAction(item = items[0], isSelected = selectedIndex == 0)
            BottomBarAction(item = items[1], isSelected = selectedIndex == 1)
            Spacer(modifier = Modifier.width(64.dp)) // Space for FAB
            BottomBarAction(item = items[2], isSelected = selectedIndex == 2)
            BottomBarAction(item = items[3], isSelected = selectedIndex == 3)
        }
        FloatingActionButton(
            onClick = { /* TODO: Main action */ },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .size(64.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-32).dp),
            elevation = FloatingActionButtonDefaults.elevation(8.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun BottomBarAction(item: BottomBarItem, isSelected: Boolean) {
    val color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    val iconAlpha = if (item.enabled) 1f else 0.3f
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent)
            .clickable(enabled = item.enabled) { /* TODO: Handle navigation/click */ }
            .padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = color,
            modifier = Modifier.size(28.dp).graphicsLayer(alpha = iconAlpha)
        )
        Text(
            text = item.label,
            color = color,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

data class BottomBarItem(val label: String, val icon: ImageVector, val enabled: Boolean = true)

// --- Legacy/Unused Composables (kept for reference) ---

/**
 * Legacy: CompactHeader composable (not used in current HomeScreen)
 */
@Composable
private fun CompactHeader(userName: String, primaryColor: Color, onPrimary: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(primaryColor)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Profile picture placeholder
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(onPrimary.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(userName.take(1), color = onPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("emote", color = onPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = 1.sp)
                    Text("CARE", color = onPrimary, fontSize = 10.sp, letterSpacing = 2.sp)
                }
                IconButton(onClick = { /* TODO: Rewards */ }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Star, contentDescription = "Rewards", tint = onPrimary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { /* TODO: Notifications */ }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = onPrimary, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(buildAnnotatedString {
                append("Hello, ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(userName) }
                append("! \uD83D\uDC4B")
            }, color = onPrimary, fontSize = 17.sp)
            Text("A great day to check your mood", color = onPrimary, fontSize = 15.sp)
        }
    }
}

/**
 * Legacy: MoodSelectionRow composable (not used in current HomeScreen)
 */
@Composable
private fun MoodSelectionRow(bgColor: Color, iconTint: Color) {
    val moods = listOf(
        "Awesome" to R.drawable.ic_happy,
        "Great" to R.drawable.ic_happy,
        "Alright" to R.drawable.ic_happy,
        "Not Great" to R.drawable.ic_happy,
        "Terrible" to R.drawable.ic_happy
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 6.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.width(10.dp))
        moods.forEach { (label, icon) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = bgColor,
                    modifier = Modifier.size(54.dp),
                    tonalElevation = 0.dp
                ) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = label,
                        modifier = Modifier.padding(13.dp).size(28.dp),
                        tint = iconTint
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(label, style = MaterialTheme.typography.labelMedium, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
    }
}

/**
 * Legacy: MoodCalendarRow composable (not used in current HomeScreen)
 */
@Composable
private fun MoodCalendarRow(bgColor: Color, textColor: Color) {
    val days = listOf(
        Triple("Mon", "04", R.drawable.ic_happy),
        Triple("Tue", "05", R.drawable.ic_happy),
        Triple("Wed", "06", R.drawable.ic_happy),
        Triple("Thu", "07", R.drawable.ic_happy), // highlighted
        Triple("Fri", "08", R.drawable.ic_happy),
        Triple("Sat", "09", R.drawable.ic_happy)
    )
    val highlightColor = bgColor
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Spacer(modifier = Modifier.width(10.dp))
        days.forEachIndexed { idx, (day, date, icon) ->
            val isToday = idx == 3
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isToday) highlightColor else Color.Transparent)
                    .padding(vertical = 7.dp, horizontal = 13.dp)
            ) {
                Text(day, fontWeight = FontWeight.Bold, color = textColor, fontSize = 15.sp)
                Text(date, fontWeight = FontWeight.Bold, color = textColor, fontSize = 15.sp)
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = day,
                    modifier = Modifier.size(22.dp),
                    tint = textColor
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
    }
}

/**
 * Legacy: MoodChecklistCard composable (not used in current HomeScreen)
 */
@Composable
private fun MoodChecklistCard(bgColor: Color, iconTint: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 14.dp)
            .height(54.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(14.dp))
            Icon(
                painter = painterResource(id = R.drawable.ic_happy),
                contentDescription = "Checklist",
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text("Drink 15 oz of water", fontWeight = FontWeight.Medium, fontSize = 16.sp, modifier = Modifier.weight(1f))
            Icon(
                painter = painterResource(id = R.drawable.ic_happy),
                contentDescription = "Checked",
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
        }
    }
}

/**
 * Legacy: HomeScreenThematicPreview composable (not used in current HomeScreen)
 */
@Preview(showBackground = true)
@Composable
private fun HomeScreenThematicPreview() {
    OrielleTheme {
        HomeScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    OrielleTheme {
        HomeScreen()
    }
}