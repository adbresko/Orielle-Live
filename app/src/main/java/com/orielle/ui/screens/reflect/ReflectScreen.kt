package com.orielle.ui.screens.reflect

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orielle.ui.util.ScreenUtils
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.orielle.R
import com.orielle.domain.model.DefaultJournalPrompts
import com.orielle.ui.theme.*
import com.orielle.ui.components.BottomNavigation

@Composable
fun ReflectScreen(
    navController: NavController,
    themeManager: com.orielle.ui.theme.ThemeManager,
    viewModel: ReflectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Refresh profile data when screen becomes visible (e.g., returning from profile settings)
    LaunchedEffect(navController.currentBackStackEntry) {
        viewModel.refreshUserProfile()
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        ReflectScreenContent(
            uiState = uiState,
            navController = navController,
            themeManager = themeManager
        )
    }
}

@Composable
private fun ReflectScreenContent(
    uiState: ReflectUiState,
    navController: NavController,
    themeManager: com.orielle.ui.theme.ThemeManager
) {
    val themeColors = getThemeColors()
    val backgroundColor = themeColors.background
    val textColor = themeColors.onBackground
    val cardColor = themeColors.surface

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
                // Left side - Journal icon (following Figma design)
                Icon(
                    painter = painterResource(id = R.drawable.icon_view_journal),
                    contentDescription = "Write journal entry",
                    modifier = Modifier
                        .size(ScreenUtils.responsiveIconSize(28.dp))
                        .clickable { navController.navigate("journal_editor") },
                    tint = textColor
                )

                Spacer(Modifier.weight(1f))

                // Right side - User avatar (matching HomeScreen implementation)
                com.orielle.ui.screens.home.UserMiniatureAvatar(
                    userProfileImageUrl = uiState.userProfileImageUrl,
                    userLocalImagePath = uiState.userLocalImagePath,
                    userSelectedAvatarId = uiState.userSelectedAvatarId,
                    userName = uiState.userName,
                    size = ScreenUtils.responsiveIconSize(40.dp),
                    backgroundColorHex = uiState.userBackgroundColorHex,
                    onClick = { navController.navigate("profile_settings") }
                )
            }
        },
        bottomBar = {
            BottomNavigation(
                navController = navController,
                themeManager = themeManager
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {

            // Top spacing to position card 50% below top bar
            Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 10))

            // Content with proper spacing - Simple working structure
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenUtils.responsivePadding() * 1.5f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding() * 3)
            ) {
                // Today's Prompt Card - works for both states
                TodaysPromptCard(
                    prompt = if (uiState.hasMoodCheckIn) uiState.todaysPrompt else "How is your inner weather?",
                    cardColor = cardColor,
                    textColor = textColor
                )

                // Primary action button - works for both states
                Button(
                    onClick = {
                        if (uiState.hasMoodCheckIn) {
                            navController.navigate("journal_editor?promptText=${uiState.todaysPrompt}")
                        } else {
                            navController.navigate("mood_check_in")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(ScreenUtils.responsivePadding()),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StillwaterTeal
                    ),
                    contentPadding = PaddingValues(vertical = ScreenUtils.responsivePadding() * 1.5f)
                ) {
                    Text(
                        text = if (uiState.hasMoodCheckIn) "Respond to Prompt" else "Begin Check-in",
                        style = Typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp
                        )
                    )
                }

                // Alternative option - FREEWRITE pathway
                Text(
                    text = "Or, start with a blank page",
                    style = Typography.bodyLarge.copy(
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("journal_editor") }
                        .padding(vertical = ScreenUtils.responsiveSpacing())
                )
            }
        }
    }
}


@Composable
private fun TodaysPromptCard(
    prompt: String,
    cardColor: Color,
    textColor: Color
) {
    val themeColors = getThemeColors()
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(ScreenUtils.responsivePadding() * 1.25f),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = ScreenUtils.responsivePadding() * 1.5f,
                vertical = ScreenUtils.responsivePadding() * 3f // More top and bottom padding inside card
            ),
            verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding() * 1.5f)
        ) {
            // Prompt text - LORA Bold 30pt with proper line spacing
            Text(
                text = prompt,
                style = Typography.headlineSmall.copy(
                    color = textColor,
                    fontFamily = Lora,
                    fontWeight = FontWeight.Bold,
                    fontSize = (30 * ScreenUtils.getTextScaleFactor()).sp,
                    lineHeight = (40 * ScreenUtils.getTextScaleFactor()).sp // More line spacing
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun InvitationStateContent(
    navController: NavController,
    cardColor: Color,
    textColor: Color
) {
    // Invitation card - contains ONLY the invitation text (matching original TodaysPromptCard structure)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(ScreenUtils.responsivePadding() * 1.25f),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = ScreenUtils.responsivePadding() * 1.5f,
                vertical = ScreenUtils.responsivePadding() * 3f // More top and bottom padding inside card
            ),
            verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding() * 1.5f)
        ) {
            // Invitation text - LORA Bold 30pt with proper line spacing (matching original prompt card)
            Text(
                text = "How is your inner weather?",
                style = Typography.headlineSmall.copy(
                    color = textColor,
                    fontFamily = Lora,
                    fontWeight = FontWeight.Bold,
                    fontSize = (30 * ScreenUtils.getTextScaleFactor()).sp,
                    lineHeight = (40 * ScreenUtils.getTextScaleFactor()).sp // More line spacing
                ),
                textAlign = TextAlign.Center
            )
        }
    }

    // Primary action button - Begin Check-in (OUTSIDE the card)
    Button(
        onClick = { navController.navigate("mood_check_in") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ScreenUtils.responsivePadding()),
        colors = ButtonDefaults.buttonColors(
            containerColor = StillwaterTeal
        ),
        contentPadding = PaddingValues(vertical = ScreenUtils.responsivePadding() * 1.5f)
    ) {
        Text(
            text = "Begin Check-in",
            style = Typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp
            )
        )
    }

    // Alternative option - FREEWRITE pathway (OUTSIDE the card)
    Text(
        text = "Or, start with a blank page",
        style = Typography.bodyLarge.copy(
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("journal_editor") }
            .padding(vertical = ScreenUtils.responsiveSpacing())
    )
}

@Composable
private fun PersonalizedPromptStateContent(
    uiState: ReflectUiState,
    navController: NavController,
    cardColor: Color,
    textColor: Color
) {
    // Today's Prompt Card - Personalized based on mood (ONLY contains the prompt text)
    TodaysPromptCard(
        prompt = uiState.todaysPrompt,
        cardColor = cardColor,
        textColor = textColor
    )

    // Primary action button - Respond to Prompt (OUTSIDE the card)
    Button(
        onClick = { navController.navigate("journal_editor?promptText=${uiState.todaysPrompt}") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(ScreenUtils.responsivePadding()),
        colors = ButtonDefaults.buttonColors(
            containerColor = StillwaterTeal
        ),
        contentPadding = PaddingValues(vertical = ScreenUtils.responsivePadding() * 1.5f)
    ) {
        Text(
            text = "Respond to Prompt",
            style = Typography.titleMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp
            )
        )
    }

    // Alternative option - FREEWRITE pathway (OUTSIDE the card)
    Text(
        text = "Or, start with a blank page",
        style = Typography.bodyLarge.copy(
            color = textColor,
            fontWeight = FontWeight.Medium,
            fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp
        ),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("journal_editor") }
            .padding(vertical = ScreenUtils.responsiveSpacing())
    )
}

@Preview(showBackground = true, name = "Reflect Screen - Invitation Light", backgroundColor = 0xFFF6F5F1)
@Composable
fun ReflectScreenInvitationLightPreview() {
    val fakeNavController = rememberNavController()
    val fakeThemeManager = com.orielle.ui.theme.ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    OrielleTheme(darkTheme = false) {
        ReflectScreenContent(
            uiState = ReflectUiState(
                hasMoodCheckIn = false,
                isLoading = false,
                userName = "Mona",
                userSelectedAvatarId = "happy"
            ),
            navController = fakeNavController,
            themeManager = fakeThemeManager
        )
    }
}

@Preview(showBackground = true, name = "Reflect Screen - Personalized Light", backgroundColor = 0xFFF6F5F1)
@Composable
fun ReflectScreenPersonalizedLightPreview() {
    val fakeNavController = rememberNavController()
    val fakeThemeManager = com.orielle.ui.theme.ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    OrielleTheme(darkTheme = false) {
        ReflectScreenContent(
            uiState = ReflectUiState(
                hasMoodCheckIn = true,
                todaysPrompt = "What is one thing on your mind today?",
                todaysMood = "Happy",
                isLoading = false,
                userName = "Mona",
                userSelectedAvatarId = "happy"
            ),
            navController = fakeNavController,
            themeManager = fakeThemeManager
        )
    }
}

@Preview(showBackground = true, name = "Reflect Screen - Invitation Dark", backgroundColor = 0xFF1A1A1A)
@Composable
fun ReflectScreenInvitationDarkPreview() {
    val fakeNavController = rememberNavController()
    val fakeThemeManager = com.orielle.ui.theme.ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    OrielleTheme(darkTheme = true) {
        ReflectScreenContent(
            uiState = ReflectUiState(
                hasMoodCheckIn = false,
                isLoading = false,
                userName = "Mona",
                userSelectedAvatarId = "happy"
            ),
            navController = fakeNavController,
            themeManager = fakeThemeManager
        )
    }
}

@Preview(showBackground = true, name = "Reflect Screen - Personalized Dark", backgroundColor = 0xFF1A1A1A)
@Composable
fun ReflectScreenPersonalizedDarkPreview() {
    val fakeNavController = rememberNavController()
    val fakeThemeManager = com.orielle.ui.theme.ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    OrielleTheme(darkTheme = true) {
        ReflectScreenContent(
            uiState = ReflectUiState(
                hasMoodCheckIn = true,
                todaysPrompt = "What is one thing on your mind today?",
                todaysMood = "Happy",
                isLoading = false,
                userName = "Mona",
                userSelectedAvatarId = "happy"
            ),
            navController = fakeNavController,
            themeManager = fakeThemeManager
        )
    }
}
