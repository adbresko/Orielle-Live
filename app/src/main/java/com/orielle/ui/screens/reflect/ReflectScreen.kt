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
import androidx.compose.material.icons.filled.Menu
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
            // Header with book icon and menu
            ReflectHeader(
                textColor = textColor,
                onSettingsClick = { navController.navigate("profile_settings") }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Content
            Column(
                modifier = Modifier.padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
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
                        color = WaterBlue,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("journal_editor") }
                        .padding(vertical = 8.dp)
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

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun ReflectHeader(
    textColor: Color,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left side - Book icon and title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Book,
                contentDescription = "Journal",
                tint = StillwaterTeal,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = "Reflect",
                style = Typography.headlineMedium.copy(
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        // Right side - Menu icon
        IconButton(onClick = onSettingsClick) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = textColor,
                modifier = Modifier.size(24.dp)
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
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
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
                shape = RoundedCornerShape(16.dp),
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
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isDark) 0.dp else 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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

@Preview(showBackground = true)
@Composable
fun ReflectScreenLightPreview() {
    val fakeThemeManager = com.orielle.ui.theme.ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    OrielleTheme(darkTheme = false) {
        ReflectScreen(navController = rememberNavController(), themeManager = fakeThemeManager)
    }
}

@Preview(showBackground = true)
@Composable
fun ReflectScreenDarkPreview() {
    val fakeThemeManager = com.orielle.ui.theme.ThemeManager(androidx.compose.ui.platform.LocalContext.current)
    OrielleTheme(darkTheme = true) {
        ReflectScreen(navController = rememberNavController(), themeManager = fakeThemeManager)
    }
}
