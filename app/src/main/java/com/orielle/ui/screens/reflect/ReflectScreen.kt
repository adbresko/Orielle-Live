package com.orielle.ui.screens.reflect

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Person
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
import com.orielle.domain.model.JournalEntry
import com.orielle.domain.model.JournalEntryType
import com.orielle.ui.theme.*
import com.orielle.ui.components.BottomNavigation
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReflectScreen(
    navController: NavController,
    themeManager: com.orielle.ui.theme.ThemeManager,
    viewModel: ReflectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = !MaterialTheme.colorScheme.background.equals(SoftSand)

    val backgroundColor = if (isDark) DarkGray else SoftSand
    val textColor = if (isDark) SoftSand else Charcoal
    val cardColor = if (isDark) Color(0xFF2A2A2A) else Color.White
    val buttonColor = if (isDark) StillwaterTeal else StillwaterTeal

    Scaffold(
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
                .verticalScroll(rememberScrollState())
        ) {
            // Header with book icon and profile
            ReflectHeader(
                textColor = textColor,
                onProfileClick = { navController.navigate("profile_settings") },
                onBookClick = { navController.navigate("journal_editor") }
            )

            Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 2))

            // Content
            Column(
                modifier = Modifier.padding(horizontal = ScreenUtils.responsivePadding() * 1.5f),
                verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding() * 2)
            ) {
                // Today's Prompt Card - Simplified and prominent
                TodaysPromptCard(
                    prompt = uiState.todaysPrompt,
                    cardColor = cardColor,
                    textColor = textColor,
                    isDark = isDark,
                    onRespondToPrompt = {
                        navController.navigate("journal_editor?promptText=${uiState.todaysPrompt}")
                    }
                )

                // Alternative option - FREEWRITE pathway
                Text(
                    text = "Or, start with a blank page",
                    style = Typography.bodyLarge.copy(
                        color = if (isDark) WaterBlue else Charcoal.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("journal_editor") }
                        .padding(vertical = ScreenUtils.responsiveSpacing())
                )

                // Look Back Module (only show if there's a past entry)
                uiState.lookBackEntry?.let { entry ->
                    LookBackModule(
                        entry = entry,
                        cardColor = cardColor,
                        textColor = textColor,
                        isDark = isDark,
                        onClick = {
                            navController.navigate("journal_detail/${entry.id}")
                        }
                    )
                }

                Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 1.5f))
            }
        }
    }
}

@Composable
private fun ReflectHeader(
    textColor: Color,
    onProfileClick: () -> Unit,
    onBookClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = ScreenUtils.responsivePadding() * 1.5f, end = ScreenUtils.responsivePadding() * 1.5f, top = ScreenUtils.responsivePadding(), bottom = ScreenUtils.responsiveSpacing()),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side - Journal icon (clickable for free writing)
        IconButton(onClick = onBookClick) {
            Image(
                painter = painterResource(id = R.drawable.reflect),
                contentDescription = "Write without prompt",
                modifier = Modifier.size(ScreenUtils.responsiveIconSize(24.dp))
            )
        }

        // Right side - Profile icon
        IconButton(onClick = onProfileClick) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = textColor,
                modifier = Modifier.size(ScreenUtils.responsiveIconSize(24.dp))
            )
        }
    }
}

@Composable
private fun TodaysPromptCard(
    prompt: String,
    cardColor: Color,
    textColor: Color,
    isDark: Boolean,
    onRespondToPrompt: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(ScreenUtils.responsivePadding() * 1.25f),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(ScreenUtils.responsivePadding() * 1.5f),
            verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding() * 1.5f)
        ) {
            // Prompt text - Simplified and prominent
            Text(
                text = prompt,
                style = Typography.headlineSmall.copy(
                    color = textColor,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )

            // Prominent teal button
            Button(
                onClick = onRespondToPrompt,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(ScreenUtils.responsivePadding()),
                colors = ButtonDefaults.buttonColors(
                    containerColor = StillwaterTeal
                ),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Text(
                    text = "Respond to Prompt",
                    style = Typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}



@Composable
private fun LookBackModule(
    entry: JournalEntry,
    cardColor: Color,
    textColor: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding())
    ) {
        Text(
            text = "From Your Past",
            style = Typography.titleMedium.copy(
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() },
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(ScreenUtils.responsivePadding()),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(ScreenUtils.responsivePadding() * 1.25f),
                verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing() * 1.5f)
            ) {
                val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                Text(
                    text = "${dateFormat.format(entry.timestamp)}...",
                    style = Typography.bodyMedium.copy(
                        color = textColor.copy(alpha = 0.7f)
                    )
                )

                Text(
                    text = entry.content.take(150) + if (entry.content.length > 150) "..." else "",
                    style = Typography.bodyMedium.copy(
                        color = textColor,
                        lineHeight = 24.sp
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Reflect Screen - Light")
@Composable
fun ReflectScreenLightPreview() {
    val fakeThemeManager = com.orielle.ui.theme.ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    OrielleTheme(darkTheme = false) {
        ReflectScreen(navController = rememberNavController(), themeManager = fakeThemeManager)
    }
}

@Preview(showBackground = true, name = "Reflect Screen - Dark")
@Composable
fun ReflectScreenDarkPreview() {
    val fakeThemeManager = com.orielle.ui.theme.ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    OrielleTheme(darkTheme = true) {
        ReflectScreen(navController = rememberNavController(), themeManager = fakeThemeManager)
    }
}
