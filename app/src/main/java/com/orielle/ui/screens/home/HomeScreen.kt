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
import com.orielle.ui.theme.Lora
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
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.EaseOutBack
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.orielle.ui.components.WaterDropLoading
import com.orielle.ui.util.ScreenUtils
import com.orielle.ui.components.BottomNavigation
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import java.io.File

// Import the WeeklyMoodView composable from the same package (auto-resolved)

// Helper function to get avatar library
private fun getAvatarLibrary(): List<com.orielle.ui.components.AvatarOption> {
    return listOf(
        com.orielle.ui.components.AvatarOption(
            id = "avatar1",
            name = "Calm",
            imageUrl = "https://example.com/avatar1.jpg",
            isPremium = false
        ),
        com.orielle.ui.components.AvatarOption(
            id = "avatar2",
            name = "Joyful",
            imageUrl = "https://example.com/avatar2.jpg",
            isPremium = false
        ),
        com.orielle.ui.components.AvatarOption(
            id = "avatar3",
            name = "Thoughtful",
            imageUrl = "https://example.com/avatar3.jpg",
            isPremium = false
        ),
        com.orielle.ui.components.AvatarOption(
            id = "avatar4",
            name = "Serene",
            imageUrl = "https://example.com/avatar4.jpg",
            isPremium = false
        ),
        com.orielle.ui.components.AvatarOption(
            id = "avatar5",
            name = "Energetic",
            imageUrl = "https://example.com/avatar5.jpg",
            isPremium = true
        ),
        com.orielle.ui.components.AvatarOption(
            id = "avatar6",
            name = "Mystical",
            imageUrl = "https://example.com/avatar6.jpg",
            isPremium = true
        )
    )
}

@Composable
private fun UserInitialAvatar(
    userName: String,
    size: androidx.compose.ui.unit.Dp
) {
    val themeColors = MaterialTheme.colorScheme
    val initial = userName.take(1).uppercase()

    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = themeColors.primary.copy(alpha = 0.2f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            style = Typography.bodyMedium.copy(
                color = themeColors.primary,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
internal fun UserMiniatureAvatar(
    userProfileImageUrl: String?,
    userLocalImagePath: String?,
    userSelectedAvatarId: String?,
    userName: String?,
    size: androidx.compose.ui.unit.Dp
) {
    val themeColors = MaterialTheme.colorScheme

    // Subtle animation states
    var isVisible by remember { mutableStateOf(false) }
    val alpha by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = EaseOutCubic),
        label = "avatarAlpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = tween(durationMillis = 300, easing = EaseOutBack),
        label = "avatarScale"
    )

    // Trigger animation when component appears
    LaunchedEffect(Unit) {
        isVisible = true
    }

    Box(
        modifier = Modifier.graphicsLayer {
            this.alpha = alpha
            scaleX = scale
            scaleY = scale
        }
    ) {
        when {
            // Priority 1: Local uploaded image
            userLocalImagePath != null && File(userLocalImagePath).exists() -> {
                Image(
                    painter = rememberAsyncImagePainter(File(userLocalImagePath)),
                    contentDescription = "Your Profile",
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            }

            // Priority 2: Remote profile image
            userProfileImageUrl != null && userProfileImageUrl.isNotBlank() -> {
                AsyncImage(
                    model = userProfileImageUrl,
                    contentDescription = "Your Profile",
                    modifier = Modifier
                        .size(size)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.ic_orielle_drop),
                    error = painterResource(id = R.drawable.ic_orielle_drop)
                )
            }

            // Priority 3: Selected avatar from library
            userSelectedAvatarId != null -> {
                val avatarLibrary = getAvatarLibrary()
                val selectedAvatar = avatarLibrary.find { it.id == userSelectedAvatarId }
                if (selectedAvatar != null) {
                    Image(
                        painter = rememberAsyncImagePainter(selectedAvatar.imageUrl),
                        contentDescription = selectedAvatar.name,
                        modifier = Modifier
                            .size(size)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Fallback to initial
                    UserInitialAvatar(userName = userName.toString(), size = size)
                }
            }

            // Priority 4: User initial in themed circle
            !userName.isNullOrBlank() -> {
                UserInitialAvatar(userName = userName, size = size)
            }

            // Fallback: Default circle
            else -> {
                Box(
                    modifier = Modifier
                        .size(size)
                        .background(
                            color = themeColors.primary.copy(alpha = 0.2f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "You",
                        modifier = Modifier.size(size * 0.6f),
                        tint = themeColors.primary
                    )
                }
            }
        }
    }
}

@Composable
fun HomeScreen(
    navController: NavController,
    themeManager: com.orielle.ui.theme.ThemeManager,
    viewModel: HomeViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dashboardState by viewModel.dashboardState.collectAsState()

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
                },
                // Pass profile data for miniature avatar
                userProfileImageUrl = uiState.userProfileImageUrl,
                userLocalImagePath = uiState.userLocalImagePath,
                userSelectedAvatarId = uiState.userSelectedAvatarId
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
    onCheckInTap: () -> Unit,
    // Profile data for miniature avatar
    userProfileImageUrl: String? = null,
    userLocalImagePath: String? = null,
    userSelectedAvatarId: String? = null
) {
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
                    .padding(
                        start = if (ScreenUtils.isSmallScreen()) 16.dp else 24.dp,
                        end = if (ScreenUtils.isSmallScreen()) 16.dp else 24.dp,
                        top = if (ScreenUtils.isSmallScreen()) 6.dp else 8.dp,
                        bottom = if (ScreenUtils.isSmallScreen()) 6.dp else 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Orielle logo and text - side by side
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_orielle_drop),
                        contentDescription = "Orielle Logo",
                        modifier = Modifier.size(20.dp)
                        // No colorFilter - using native colors for consistency
                    )
                    Text(
                        text = "ORIELLE",
                        style = Typography.bodySmall.copy(
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = Lora,
                            fontSize = 22.sp
                        )
                    )
                }
                Spacer(Modifier.weight(1f))
                // Profile section with avatar on the right (leading UX practice)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Profile icon (left)
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile",
                        modifier = Modifier
                            .size(28.dp)
                            .clickable { navController.navigate("profile_settings") },
                        tint = MaterialTheme.colorScheme.onBackground
                    )

                    // Miniature user avatar (right - following leading UX practices)
                    UserMiniatureAvatar(
                        userProfileImageUrl = userProfileImageUrl,
                        userLocalImagePath = userLocalImagePath,
                        userSelectedAvatarId = userSelectedAvatarId,
                        userName = userName,
                        size = ScreenUtils.responsiveIconSize(24.dp)
                    )
                }
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
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                            style = Typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
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
                    // Dynamic greeting based on time of day
                    val greeting = remember {
                        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                        when {
                            hour < 12 -> "Good morning"
                            hour < 17 -> "Good afternoon"
                            else -> "Good evening"
                        }
                    }

                    Text(
                        text = "$greeting, ${userName ?: "User"}.",
                        style = Typography.headlineLarge.copy(color = textColor),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    // Dynamic date line with moon phase - directly under greeting
                    var moonPhaseDisplay by remember { mutableStateOf("🌙 waxing crescent") }

                    // Load moon phase
                    LaunchedEffect(Unit) {
                        moonPhaseDisplay = try {
                            val moonPhase = com.orielle.util.LocalMoonPhaseUtils.getMoonPhase(today)
                            "🌙 ${moonPhase.phase}"
                        } catch (e: Exception) {
                            "🌙 waxing crescent" // Fallback
                        }
                    }

                    Text(
                        text = "${dateFormat.format(today)}. $moonPhaseDisplay | ${today.year + 1900}",
                        style = Typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            fontSize = 15.sp // Larger font for better readability
                        ),
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 3))

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
                                onClick = { navController.navigate("inner_weather_history") }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "YOUR INNER WEATHER",
                                style = Typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                                textAlign = TextAlign.Start
                            )
                            Spacer(Modifier.height(ScreenUtils.responsivePadding()))
                            WeeklyMoodView(
                                weeklyView = weeklyMoodView,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 2))
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
                                onClick = { navController.navigate("reflect") }
                            ),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "A THOUGHT FROM YOUR PAST",
                                style = Typography.titleMedium.copy(color = accentColor),
                                textAlign = TextAlign.Start
                            )
                            Spacer(Modifier.height(12.dp))
                            // Random thought selection
                            val randomThought = remember(journalEntries) {
                                if (journalEntries.isNotEmpty()) {
                                    journalEntries.random().content
                                } else {
                                    "Felt a real sense of growth today after that challenging conversation."
                                }
                            }
                            Text(
                                text = randomThought,
                                style = Typography.bodyMedium.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
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
    val unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)

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
    com.orielle.ui.theme.OrielleThemeWithPreference(themeManager = fakeThemeManager) {
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
    // Force dark theme for this preview
    com.orielle.ui.theme.OrielleTheme(darkTheme = true) {
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
    com.orielle.ui.theme.OrielleThemeWithPreference(themeManager = fakeThemeManager) {
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
    // Force dark theme for this preview
    com.orielle.ui.theme.OrielleTheme(darkTheme = true) {
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
