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

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dashboardState by viewModel.dashboardState.collectAsState()
    HomeDashboardScreen(
        userName = uiState.userName,
        journalEntries = uiState.journalEntries,
        weeklyMoodView = uiState.weeklyMoodView,
        navController = navController,
        dashboardState = dashboardState,
        onCheckInTap = { viewModel.onCheckInCompletedOrSkipped() }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun HomeDashboardScreen(
    userName: String?,
    journalEntries: List<com.orielle.domain.model.JournalEntry>,
    weeklyMoodView: com.orielle.domain.model.WeeklyMoodView,
    navController: NavController,
    dashboardState: DashboardState,
    onCheckInTap: () -> Unit,
    profileImageUrl: String? = null
) {
    val isDark = MaterialTheme.colorScheme.background == DarkGray
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val accentColor = WaterBlue
    val today = remember { Date() }
    val dateFormat = remember { SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()) }
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
                    .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Orielle logo and text
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_orielle_drop),
                        contentDescription = "Orielle Logo",
                        modifier = Modifier.size(28.dp),
                        tint = accentColor
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "ORIELLE",
                        style = Typography.bodyLarge.copy(color = textColor, fontWeight = FontWeight.Normal)
                    )
                }
                Spacer(Modifier.weight(1f))
                // Profile icon
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { navController.navigate("profile_settings") },
                    tint = if (isDark) SoftSand else Charcoal
                )
            }
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(backgroundColor)
                    .padding(bottom = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DashboardNavItem(
                        icon = R.drawable.home,
                        label = "Home",
                        selected = true,
                        onClick = { /* Already on home */ }
                    )
                    DashboardNavItem(
                        icon = R.drawable.reflect,
                        label = "Reflect",
                        selected = false,
                        onClick = { /* TODO: Navigate to reflect */ }
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
                        selected = false,
                        onClick = { /* TODO: Navigate to remember */ }
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))
            AnimatedVisibility(
                visible = dashboardState == DashboardState.Initial,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
            ) {
                // State 1: Pre-check-in
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
                    colors = CardDefaults.cardColors(containerColor = if (isDark) DarkGray else SoftSand),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .scale(breathingScale)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_orielle_drop),
                                contentDescription = "Water Drop",
                                modifier = Modifier.fillMaxSize(),
                                tint = accentColor
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "How is your inner weather ?",
                            style = Typography.headlineLarge.copy(color = textColor),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Tap here to begin your check-in.",
                            style = Typography.bodyLarge.copy(color = if (isDark) SoftSand else Charcoal),
                            textAlign = TextAlign.Center
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
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Good morning, ${userName ?: "User"}.",
                        style = Typography.displayLarge.copy(color = textColor),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "${dateFormat.format(today)} • waxing crescent 311 • Day 204",
                        style = Typography.bodyLarge.copy(color = if (isDark) SoftSand else Charcoal),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
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
                                onClick = { navController.navigate("remember") }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) DarkGray else SoftSand),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 24.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            WeeklyMoodView(
                                weeklyView = weeklyMoodView,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                    // Thought From Your Past Card
                    var thoughtPressed by remember { mutableStateOf(false) }
                    val thoughtScale by animateFloatAsState(targetValue = if (thoughtPressed) 0.95f else 1f, label = "thoughtTapScale")
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
                                onClick = { navController.navigate("remember") }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) DarkGray else SoftSand),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 32.dp, horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "A THOUGHT FROM YOUR PAST",
                                style = Typography.titleLarge.copy(color = accentColor),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))
                            val quote = journalEntries.lastOrNull()?.content ?: "Felt a real sense of growth today after that challenging conversation."
                            Text(
                                text = quote,
                                style = Typography.bodyLarge.copy(
                                    color = if (isDark) SoftSand else Charcoal,
                                    fontStyle = FontStyle.Italic
                                ),
                                textAlign = TextAlign.Center
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
    val isDark = MaterialTheme.colorScheme.background == DarkGray
    val unselectedTextColor = if (isDark) Color.LightGray else Charcoal.copy(alpha = 0.7f)

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
            onCheckInTap = {},
            profileImageUrl = null
        )
    }
}

@Preview(name = "State 1 - Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun Preview_HomeDashboard_Initial_Dark() {
    val fakeNavController = androidx.navigation.compose.rememberNavController()
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
            onCheckInTap = {},
            profileImageUrl = null
        )
    }
}

@Preview(name = "State 2 - Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_HomeDashboard_Unfolded_Light() {
    val fakeNavController = androidx.navigation.compose.rememberNavController()
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
            onCheckInTap = {},
            profileImageUrl = null
        )
    }
}

@Preview(name = "State 2 - Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun Preview_HomeDashboard_Unfolded_Dark() {
    val fakeNavController = androidx.navigation.compose.rememberNavController()
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
            onCheckInTap = {},
            profileImageUrl = null
        )
    }
}