package com.orielle.ui.screens.reflect

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReflectScreen(
    navController: NavController,
    viewModel: ReflectViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = !MaterialTheme.colorScheme.background.equals(SoftSand)

    val backgroundColor = if (isDark) DarkGray else SoftSand
    val textColor = if (isDark) SoftSand else Charcoal
    val cardColor = if (isDark) Color(0xFF2A2A2A) else Color.White

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        ReflectHeader(
            isDark = isDark,
            textColor = textColor,
            onSettingsClick = { navController.navigate("profile_settings") },
            onJournalLogClick = { navController.navigate("journal_log") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Content
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Today's Prompt Card
            TodaysPromptCard(
                prompt = uiState.todaysPrompt,
                cardColor = cardColor,
                textColor = textColor,
                isDark = isDark,
                onClick = {
                    navController.navigate("journal_editor?promptText=${uiState.todaysPrompt}")
                }
            )

            // Action Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Free write",
                    subtitle = "Your blank canvas",
                    icon = R.drawable.reflect,
                    cardColor = cardColor,
                    textColor = textColor,
                    onClick = { navController.navigate("journal_editor") }
                )

                ActionCard(
                    modifier = Modifier.weight(1f),
                    title = "Quick entry",
                    subtitle = "A single thought",
                    icon = R.drawable.remember,
                    cardColor = cardColor,
                    textColor = textColor,
                    onClick = { navController.navigate("journal_editor?isQuickEntry=true") }
                )
            }

            // Look Back Module (only show if there's a past entry)
            uiState.lookBackEntry?.let { entry ->
                LookBackModule(
                    entry = entry,
                    cardColor = cardColor,
                    textColor = textColor,
                    onClick = {
                        navController.navigate("journal_detail/${entry.id}")
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ReflectHeader(
    isDark: Boolean,
    textColor: Color,
    onSettingsClick: () -> Unit,
    onJournalLogClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Note Your Thoughts",
            style = Typography.headlineMedium.copy(
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = textColor
                )
            }

            IconButton(onClick = onJournalLogClick) {
                Image(
                    painter = painterResource(id = R.drawable.remember),
                    contentDescription = "View Journal",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun TodaysPromptCard(
    prompt: String,
    cardColor: Color,
    textColor: Color,
    isDark: Boolean,
    onClick: () -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "✨",
                    fontSize = 20.sp
                )
                Text(
                    text = "Today's Prompt",
                    style = Typography.titleMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }

            Text(
                text = prompt,
                style = Typography.bodyLarge.copy(
                    color = textColor,
                    lineHeight = 24.sp
                )
            )
        }
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: Int,
    cardColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = title,
                modifier = Modifier.size(32.dp)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = Typography.titleSmall.copy(
                        color = textColor,
                        fontWeight = FontWeight.SemiBold
                    ),
                    textAlign = TextAlign.Center
                )

                Text(
                    text = subtitle,
                    style = Typography.bodySmall.copy(
                        color = textColor.copy(alpha = 0.7f)
                    ),
                    textAlign = TextAlign.Center
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
    onClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        lineHeight = 20.sp
                    )
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ReflectScreenLightPreview() {
    OrielleTheme(darkTheme = false) {
        ReflectScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true)
@Composable
fun ReflectScreenDarkPreview() {
    OrielleTheme(darkTheme = true) {
        ReflectScreen(navController = rememberNavController())
    }
}
