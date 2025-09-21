package com.orielle.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.orielle.ui.theme.DarkGray
import com.orielle.ui.theme.WaterBlue
import com.orielle.ui.theme.Typography
import com.orielle.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.rememberAsyncImagePainter
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.sp
import com.orielle.ui.theme.SoftSand
import com.orielle.ui.theme.Charcoal
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.ui.input.pointer.pointerInteropFilter
import android.view.MotionEvent
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.ExperimentalComposeUiApi
import com.orielle.ui.theme.OrielleTheme
import com.orielle.ui.theme.getThemeColors
import com.orielle.ui.components.WaterDropLoading
import com.orielle.ui.util.ScreenUtils
import com.orielle.ui.components.BottomNavigation
import com.orielle.util.GreetingUtils
import com.orielle.util.MoonPhaseUtils

// Import the WeeklyMoodView composable from the same package (auto-resolved)

@Composable
fun HomeScreen(
    navController: NavController,
    themeManager: com.orielle.ui.theme.ThemeManager,
    viewModel: HomeViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dashboardState by viewModel.dashboardState.collectAsState()
    val themeColors = getThemeColors()
    val isDark = themeColors.isDark

    // Refresh data when returning to home screen
    LaunchedEffect(Unit) {
        viewModel.refreshHomeData()
    }

    // Show loading indicator during initialization to prevent flicker
    if (uiState.isInitializing || dashboardState == DashboardState.Loading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                WaterDropLoading(
                    size = ScreenUtils.responsiveImageSize(60.dp).value.toInt(),
                    modifier = Modifier.size(ScreenUtils.responsiveImageSize(60.dp))
                )
                Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing() * 2))
                Text(
                    text = if (uiState.isInitializing) "Loading your dashboard..." else "Preparing your day...",
                    style = Typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        // Smooth transition when dashboard state changes
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(animationSpec = tween(300)) + slideInVertically(
                animationSpec = tween(300),
                initialOffsetY = { it / 4 }
            )
        ) {
            HomeDashboardScreen(
                userName = uiState.userName,
                journalEntries = uiState.journalEntries,
                weeklyMoodView = uiState.weeklyMoodView,
                navController = navController,
                dashboardState = dashboardState,
                themeManager = themeManager,
                onCheckInTap = {
                    // Navigate to mood check-in screen instead of just changing state
                    navController.navigate("mood_check_in")
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeDashboardScreen(
    userName: String?,
    journalEntries: List<com.orielle.domain.model.JournalEntry>,
    weeklyMoodView: com.orielle.domain.model.WeeklyMoodView,
    navController: NavController,
    dashboardState: DashboardState,
    themeManager: com.orielle.ui.theme.ThemeManager,
    onCheckInTap: () -> Unit
) {
    val themeColors = getThemeColors()
    val backgroundColor = themeColors.background
    val textColor = themeColors.onBackground
    val accentColor = WaterBlue
    val today = remember { Date() }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }
    val yearFormat = remember { SimpleDateFormat("yyyy", Locale.getDefault()) }
    // Moon phase using local calculations (reliable and fast)
    val moonPhaseDisplay = remember(today) {
        try {
            // Use runBlocking for synchronous call since we're in remember
            kotlinx.coroutines.runBlocking {
                MoonPhaseUtils.getMoonPhaseDisplay(today)
            }
        } catch (e: Exception) {
            "🌙 Unavailable"
        }
    }
    val breathingTransition = rememberInfiniteTransition(label = "breathing")
    val breathingScale by breathingTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000),
            repeatMode = RepeatMode.Reverse
        ), label = "breathingScale"
    )
    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = if (ScreenUtils.isSmallScreen()) 16.dp else 24.dp,
                        end = if (ScreenUtils.isSmallScreen()) 16.dp else 24.dp,
                        top = if (ScreenUtils.isSmallScreen()) 6.dp else 8.dp,
                        bottom = if (ScreenUtils.isSmallScreen()) 6.dp else 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Water drop icon and ORIELLE on the left
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_orielle_drop),
                        contentDescription = "Orielle Logo",
                        modifier = Modifier.size(20.dp)
                        // No colorFilter - using native colors for consistency
                    )
                    Text(
                        text = "ORIELLE",
                        style = Typography.titleMedium.copy(color = textColor, fontWeight = FontWeight.Medium)
                    )
                }
                Spacer(Modifier.weight(1f))
                // Profile icon on the right
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { navController.navigate("profile_settings") },
                    tint = themeColors.onBackground
                )
            }
        },
        bottomBar = {
            BottomNavigation(
                navController = navController,
                themeManager = themeManager
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = if (ScreenUtils.isSmallScreen()) 16.dp else 24.dp),
            verticalArrangement = if (dashboardState == DashboardState.Initial) Arrangement.Center else Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (dashboardState == DashboardState.Unfolded) {
                Spacer(Modifier.height(if (ScreenUtils.isSmallScreen()) 36.dp else 48.dp))
            }
            AnimatedVisibility(
                visible = dashboardState == DashboardState.Initial,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
            ) {
                // State 1: Pre-check-in - ONLY the check-in card, no greeting or date
                var pressed by remember { mutableStateOf(false) }
                val scale by animateFloatAsState(targetValue = if (pressed) 0.95f else 1f, label = "tapScale")
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pointerInteropFilter {
                            when (it.action) {
                                MotionEvent.ACTION_DOWN -> { pressed = true }
                                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { pressed = false }
                            }
                            false // Let .clickable handle the click
                        }
                        .scale(scale)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = rememberRipple(bounded = true, color = accentColor),
                            onClick = onCheckInTap
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (themeColors.isDark) 0.dp else 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(ScreenUtils.responsiveImageSize(64.dp))
                                .scale(breathingScale)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_orielle_drop),
                                contentDescription = "Water Drop",
                                modifier = Modifier.fillMaxSize()
                                // No colorFilter - using native colors for consistency
                            )
                        }
                        Spacer(Modifier.height(ScreenUtils.responsivePadding()))
                        Text(
                            text = "How is your inner weather ?",
                            style = Typography.titleLarge.copy(color = textColor),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(ScreenUtils.responsiveSpacing()))
                        Text(
                            text = "Tap here to begin your check-in.",
                            style = Typography.bodyLarge.copy(color = themeColors.onBackground),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = dashboardState == DashboardState.Unfolded,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
            ) {
                // State 2: Post-check-in
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Greeting - left aligned
                    Text(
                        text = GreetingUtils.getTimeBasedGreeting(userName, today),
                        style = Typography.headlineLarge.copy(color = textColor),
                        modifier = Modifier.padding(bottom = ScreenUtils.responsivePadding() * 1.5f)
                    )

                    // Date line - UNDER the greeting, smaller font
                    Text(
                        text = "${dateFormat.format(today)} • ${moonPhaseDisplay} | ${yearFormat.format(today)}",
                        style = Typography.bodyMedium.copy(
                            color = themeColors.onBackground,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(bottom = ScreenUtils.responsivePadding() * 1f)
                    )

                    // Spacer before cards - reduced spacing
                    Spacer(Modifier.height(ScreenUtils.responsivePadding() * 1f))

                    // Inner Weather Card
                    var weatherPressed by remember { mutableStateOf(false) }
                    val weatherScale by animateFloatAsState(targetValue = if (weatherPressed) 0.95f else 1f, label = "weatherTapScale")
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInteropFilter {
                                when (it.action) {
                                    MotionEvent.ACTION_DOWN -> { weatherPressed = true }
                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { weatherPressed = false }
                                }
                                false
                            }
                            .scale(weatherScale)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(bounded = true, color = accentColor),
                                onClick = { navController.navigate("reflect") }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (themeColors.isDark) 0.dp else 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            WeeklyMoodView(
                                weeklyView = weeklyMoodView,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(Modifier.height(ScreenUtils.responsivePadding() * 3f))
                    // Thought From Your Past Card
                    var thoughtPressed by remember { mutableStateOf(false) }
                    val thoughtScale by animateFloatAsState(targetValue = if (thoughtPressed) 0.95f else 1f, label = "thoughtTapScale")

                    // Select a random journal entry for the card (only meaningful content)
                    val selectedEntry = if (journalEntries.isNotEmpty()) {
                        val meaningfulEntries = journalEntries.filter { entry ->
                            entry.content.trim().length >= 5
                        }
                        if (meaningfulEntries.isNotEmpty()) {
                            meaningfulEntries.random()
                        } else {
                            null
                        }
                    } else {
                        null
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerInteropFilter {
                                when (it.action) {
                                    MotionEvent.ACTION_DOWN -> { thoughtPressed = true }
                                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> { thoughtPressed = false }
                                }
                                false
                            }
                            .scale(thoughtScale)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(bounded = true, color = accentColor),
                                onClick = {
                                    if (selectedEntry != null) {
                                        navController.navigate("journal_detail/${selectedEntry.id}")
                                    } else {
                                        navController.navigate("reflect")
                                    }
                                }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = themeColors.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (themeColors.isDark) 0.dp else 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 20.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "A THOUGHT FROM YOUR PAST",
                                style = Typography.titleMedium.copy(color = themeColors.onSurface, fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Start
                            )
                            Spacer(Modifier.height(12.dp))
                            val quote = selectedEntry?.content ?: "This space is empty... Take a moment to reflect on your day and capture your thoughts. Your future self will thank you for these meaningful moments."
                            Text(
                                text = quote,
                                style = Typography.bodyMedium.copy(
                                    color = themeColors.onSurface,
                                    fontStyle = FontStyle.Italic
                                ),
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardNavItem(icon: Int, label: String, selected: Boolean, onClick: () -> Unit = {}) {
    val themeColors = getThemeColors()
    val unselectedTextColor = if (themeColors.isDark) Color.LightGray else Charcoal

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
            style = Typography.bodyMedium.copy(color = if (selected) WaterBlue else unselectedTextColor)
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .height(2.dp)
                    .width(32.dp)
                    .background(WaterBlue)
            )
        }
    }
}

@Preview(name = "State 1 - Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_HomeDashboard_Initial_Light() {
    val fakeNavController = androidx.navigation.compose.rememberNavController()
    val fakeThemeManager = com.orielle.ui.theme.ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    OrielleTheme(darkTheme = false) {
        HomeDashboardScreen(
            userName = "Mona",
            journalEntries = listOf(
                com.orielle.domain.model.JournalEntry(
                    id = "1",
                    userId = "user1",
                    content = "Felt a real sense of growth today after that challenging conversation.",
                    mood = "Reflective"
                )
            ),
            weeklyMoodView = com.orielle.domain.model.WeeklyMoodView(emptyList(), 0),
            navController = fakeNavController,
            dashboardState = DashboardState.Initial,
            themeManager = fakeThemeManager,
            onCheckInTap = {}
        )
    }
}

@Preview(name = "State 1 - Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun Preview_HomeDashboard_Initial_Dark() {
    val fakeNavController = androidx.navigation.compose.rememberNavController()
    val fakeThemeManager = com.orielle.ui.theme.ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    OrielleTheme(darkTheme = true) {
        HomeDashboardScreen(
            userName = "Mona",
            journalEntries = listOf(
                com.orielle.domain.model.JournalEntry(
                    id = "1",
                    userId = "user1",
                    content = "Felt a real sense of growth today after that challenging conversation.",
                    mood = "Reflective"
                )
            ),
            weeklyMoodView = com.orielle.domain.model.WeeklyMoodView(emptyList(), 0),
            navController = fakeNavController,
            dashboardState = DashboardState.Initial,
            themeManager = fakeThemeManager,
            onCheckInTap = {}
        )
    }
}

@Preview(name = "State 2 - Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_HomeDashboard_Unfolded_Light() {
    val fakeNavController = androidx.navigation.compose.rememberNavController()
    val fakeThemeManager = com.orielle.ui.theme.ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    OrielleTheme(darkTheme = false) {
        HomeDashboardScreen(
            userName = "Mona",
            journalEntries = listOf(
                com.orielle.domain.model.JournalEntry(
                    id = "1",
                    userId = "user1",
                    content = "Felt a real sense of growth today after that challenging conversation.",
                    mood = "Reflective"
                )
            ),
            weeklyMoodView = com.orielle.domain.model.WeeklyMoodView(emptyList(), 0),
            navController = fakeNavController,
            dashboardState = DashboardState.Unfolded,
            themeManager = fakeThemeManager,
            onCheckInTap = {}
        )
    }
}

@Preview(name = "State 2 - Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun Preview_HomeDashboard_Unfolded_Dark() {
    val fakeNavController = androidx.navigation.compose.rememberNavController()
    val fakeThemeManager = com.orielle.ui.theme.ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    OrielleTheme(darkTheme = true) {
        HomeDashboardScreen(
            userName = "Mona",
            journalEntries = listOf(
                com.orielle.domain.model.JournalEntry(
                    id = "1",
                    userId = "user1",
                    content = "Felt a real sense of growth today after that challenging conversation.",
                    mood = "Reflective"
                )
            ),
            weeklyMoodView = com.orielle.domain.model.WeeklyMoodView(emptyList(), 0),
            navController = fakeNavController,
            dashboardState = DashboardState.Unfolded,
            themeManager = fakeThemeManager,
            onCheckInTap = {}
        )
    }
}