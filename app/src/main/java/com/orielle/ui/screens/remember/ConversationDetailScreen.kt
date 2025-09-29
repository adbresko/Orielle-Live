package com.orielle.ui.screens.remember

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.orielle.ui.util.ScreenUtils
import com.orielle.ui.components.WaterDropLoading
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.orielle.ui.theme.*
import com.orielle.domain.model.ChatMessage
import com.orielle.domain.model.ChatConversation
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.res.painterResource
import com.orielle.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationDetailScreen(
    navController: NavController,
    conversationId: String,
    viewModel: ConversationDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(conversationId) {
        viewModel.loadConversation(conversationId)
    }

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
                // Left side - Back button
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor,
                        modifier = Modifier.size(ScreenUtils.responsiveIconSize(24.dp))
                    )
                }

                Spacer(Modifier.weight(1f))

                // Center - Title
                val dateText = formatConversationDate(uiState.conversation?.createdAt)
                val dynamicFontSize = remember(dateText) {
                    // Base font size
                    val baseSize = 24f
                    // Scale down if text is long (more than 15 characters)
                    when {
                        dateText.length > 20 -> baseSize * 0.7f
                        dateText.length > 15 -> baseSize * 0.8f
                        else -> baseSize
                    }
                }

                Text(
                    text = dateText,
                    fontFamily = Lora,
                    fontSize = (dynamicFontSize * ScreenUtils.getTextScaleFactor()).sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(2f)
                )

                Spacer(Modifier.weight(1f))

                // Right side - Empty space for balance
                Spacer(modifier = Modifier.size(ScreenUtils.responsiveIconSize(24.dp)))
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = backgroundColor)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WaterDropLoading(
                        size = ScreenUtils.responsiveImageSize(60.dp).value.toInt(),
                        modifier = Modifier.size(ScreenUtils.responsiveImageSize(60.dp))
                    )
                    Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing() * 2))
                    Text(
                        text = "Loading conversation...",
                        style = Typography.bodyMedium.copy(color = textColor),
                        textAlign = TextAlign.Center,
                        fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp
                    )
                }
            }
        } else if (uiState.error != null) {
            // Error state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = backgroundColor)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing())
                ) {
                    Text(
                        text = "⚠️",
                        fontSize = (48 * ScreenUtils.getTextScaleFactor()).sp
                    )
                    Text(
                        text = "Failed to load conversation",
                        style = Typography.headlineSmall.copy(color = textColor),
                        textAlign = TextAlign.Center,
                        fontSize = (20 * ScreenUtils.getTextScaleFactor()).sp
                    )
                    Text(
                        text = uiState.error ?: "Unknown error",
                        style = Typography.bodyMedium.copy(color = textColor.copy(alpha = 0.7f)),
                        textAlign = TextAlign.Center,
                        fontSize = (14 * ScreenUtils.getTextScaleFactor()).sp
                    )
                    Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing()))
                    Button(
                        onClick = {
                            // Retry loading
                            viewModel.loadConversation(conversationId)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StillwaterTeal),
                        modifier = Modifier.padding(ScreenUtils.responsivePadding())
                    ) {
                        Text(
                            "Retry",
                            color = Color.White,
                            fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = backgroundColor)
                    .padding(paddingValues)
                    .padding(horizontal = ScreenUtils.responsivePadding()),
                verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding()),
                contentPadding = PaddingValues(vertical = ScreenUtils.responsivePadding())
            ) {
                items(
                    items = uiState.messages,
                    key = { it.id }
                ) { message ->
                    MessageBubble(message = message)
                }

                // Tags section at the bottom
                uiState.conversation?.let { conversation ->
                    android.util.Log.d("ConversationDetailScreen", "Conversation tags: ${conversation.tags}, isEmpty: ${conversation.tags.isEmpty()}")
                    if (conversation.tags.isNotEmpty()) {
                        item {
                            TagsSection(tags = conversation.tags)
                        }
                    } else {
                        android.util.Log.d("ConversationDetailScreen", "No tags to display for conversation: ${conversation.id}")
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.isFromUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Water drop icon for Orielle messages
            Image(
                painter = painterResource(id = R.drawable.ic_orielle_drop),
                contentDescription = "Orielle",
                modifier = Modifier
                    .size(ScreenUtils.responsiveIconSize(24.dp))
                    .padding(end = 8.dp, top = 4.dp)
            )
        }

        Card(
            modifier = Modifier.widthIn(max = ScreenUtils.responsivePadding() * 17.5f),
            shape = RoundedCornerShape(ScreenUtils.responsivePadding()),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) WaterBlue else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = message.content,
                fontFamily = NotoSans,
                fontSize = 14.sp,
                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp,
                modifier = Modifier.padding(ScreenUtils.responsivePadding())
            )
        }
    }
}

@Composable
private fun TagsSection(tags: List<String>) {
    val themeColors = getThemeColors()
    val textColor = themeColors.onBackground
    val cardColor = themeColors.surface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = ScreenUtils.responsivePadding() * 2,
                bottom = ScreenUtils.responsivePadding()
            )
    ) {
        // Tags header with icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing())
        ) {
            Text(
                text = "🏷️",
                fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp
            )
            Text(
                text = "Tags",
                fontFamily = NotoSans,
                fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }

        Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing()))

        // Tags in a flexible wrap layout
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(tags) { tag ->
                Card(
                    shape = RoundedCornerShape(ScreenUtils.responsiveSpacing() * 2),
                    colors = CardDefaults.cardColors(
                        containerColor = StillwaterTeal.copy(alpha = 0.1f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        StillwaterTeal.copy(alpha = 0.3f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Text(
                        text = tag,
                        fontFamily = NotoSans,
                        fontSize = (12 * ScreenUtils.getTextScaleFactor()).sp,
                        color = StillwaterTeal,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(
                            horizontal = ScreenUtils.responsiveSpacing() * 1.5f,
                            vertical = ScreenUtils.responsiveSpacing() * 0.75f
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ConversationDetailContent(
    conversation: ChatConversation?,
    messages: List<ChatMessage>,
    isLoading: Boolean,
    error: String?,
    navController: NavController
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
                // Left side - Back button
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor,
                        modifier = Modifier.size(ScreenUtils.responsiveIconSize(24.dp))
                    )
                }

                Spacer(Modifier.weight(1f))

                // Center - Title
                val dateText = formatConversationDate(conversation?.createdAt)
                val dynamicFontSize = remember(dateText) {
                    // Base font size
                    val baseSize = 24f
                    // Scale down if text is long (more than 15 characters)
                    when {
                        dateText.length > 20 -> baseSize * 0.7f
                        dateText.length > 15 -> baseSize * 0.8f
                        else -> baseSize
                    }
                }

                Text(
                    text = dateText,
                    fontFamily = Lora,
                    fontSize = (dynamicFontSize * ScreenUtils.getTextScaleFactor()).sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(2f)
                )

                Spacer(Modifier.weight(1f))

                // Right side - Empty space for balance
                Spacer(modifier = Modifier.size(ScreenUtils.responsiveIconSize(24.dp)))
            }
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = backgroundColor)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    WaterDropLoading(
                        size = ScreenUtils.responsiveImageSize(60.dp).value.toInt(),
                        modifier = Modifier.size(ScreenUtils.responsiveImageSize(60.dp))
                    )
                    Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing() * 2))
                    Text(
                        text = "Loading conversation...",
                        style = Typography.bodyMedium.copy(color = textColor),
                        textAlign = TextAlign.Center,
                        fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp
                    )
                }
            }
        } else if (error != null) {
            // Error state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = backgroundColor)
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing())
                ) {
                    Text(
                        text = "⚠️",
                        fontSize = (48 * ScreenUtils.getTextScaleFactor()).sp
                    )
                    Text(
                        text = "Failed to load conversation",
                        style = Typography.headlineSmall.copy(color = textColor),
                        textAlign = TextAlign.Center,
                        fontSize = (20 * ScreenUtils.getTextScaleFactor()).sp
                    )
                    Text(
                        text = error,
                        style = Typography.bodyMedium.copy(color = textColor.copy(alpha = 0.7f)),
                        textAlign = TextAlign.Center,
                        fontSize = (14 * ScreenUtils.getTextScaleFactor()).sp
                    )
                    Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing()))
                    Button(
                        onClick = {
                            // Retry loading - this would need to be passed as a parameter
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = StillwaterTeal),
                        modifier = Modifier.padding(ScreenUtils.responsivePadding())
                    ) {
                        Text(
                            "Retry",
                            color = Color.White,
                            fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = backgroundColor)
                    .padding(paddingValues)
                    .padding(horizontal = ScreenUtils.responsivePadding()),
                verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding()),
                contentPadding = PaddingValues(vertical = ScreenUtils.responsivePadding())
            ) {
                items(
                    items = messages,
                    key = { it.id }
                ) { message ->
                    MessageBubble(message = message)
                }

                // Tags section at the bottom
                conversation?.let { conv ->
                    android.util.Log.d("ConversationDetailContent", "Conversation tags: ${conv.tags}, isEmpty: ${conv.tags.isEmpty()}")
                    if (conv.tags.isNotEmpty()) {
                        item {
                            TagsSection(tags = conv.tags)
                        }
                    } else {
                        android.util.Log.d("ConversationDetailContent", "No tags to display for conversation: ${conv.id}")
                    }
                }
            }
        }
    }
}

// Helper function to format conversation date
private fun formatConversationDate(date: Date?): String {
    if (date == null) return "Chat"

    val calendar = Calendar.getInstance()
    calendar.time = date

    val month = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val year = calendar.get(Calendar.YEAR)

    return "$month $day, $year"
}

@Preview(name = "Conversation Detail - Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_ConversationDetailScreen_Light() {
    val fakeNavController = androidx.navigation.compose.rememberNavController()

    OrielleThemeWithPreference(themeManager = com.orielle.ui.theme.ThemeManager(androidx.compose.ui.platform.LocalContext.current)) {
        ConversationDetailContent(
            conversation = ChatConversation(
                id = "1",
                userId = "user1",
                title = "Feeling anxious about work",
                tags = listOf("anxiety", "work", "stress"),
                createdAt = Date(),
                updatedAt = Date()
            ),
            messages = listOf(
                ChatMessage(
                    id = "1",
                    conversationId = "1",
                    content = "I'm feeling really anxious about my presentation tomorrow. I keep thinking about all the things that could go wrong.",
                    isFromUser = true,
                    timestamp = Date(System.currentTimeMillis() - 3600000)
                ),
                ChatMessage(
                    id = "2",
                    conversationId = "1",
                    content = "I understand that work presentations can feel overwhelming. It's completely natural to feel anxious about something important to you. Let's explore what's making you feel this way. What specific aspects of the presentation are causing you the most concern?",
                    isFromUser = false,
                    timestamp = Date(System.currentTimeMillis() - 3500000)
                ),
                ChatMessage(
                    id = "3",
                    conversationId = "1",
                    content = "I'm worried I'll forget my key points and look unprepared. Also, what if they ask questions I can't answer?",
                    isFromUser = true,
                    timestamp = Date(System.currentTimeMillis() - 3400000)
                )
            ),
            isLoading = false,
            error = null,
            navController = fakeNavController
        )
    }
}

@Preview(name = "Conversation Detail - Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun Preview_ConversationDetailScreen_Dark() {
    val fakeNavController = androidx.navigation.compose.rememberNavController()

    OrielleTheme(darkTheme = true) {
        ConversationDetailContent(
            conversation = ChatConversation(
                id = "1",
                userId = "user1",
                title = "Feeling anxious about work",
                tags = listOf("anxiety", "work", "stress"),
                createdAt = Date(),
                updatedAt = Date()
            ),
            messages = listOf(
                ChatMessage(
                    id = "1",
                    conversationId = "1",
                    content = "I'm feeling really anxious about my presentation tomorrow. I keep thinking about all the things that could go wrong.",
                    isFromUser = true,
                    timestamp = Date(System.currentTimeMillis() - 3600000)
                ),
                ChatMessage(
                    id = "2",
                    conversationId = "1",
                    content = "I understand that work presentations can feel overwhelming. It's completely natural to feel anxious about something important to you. Let's explore what's making you feel this way. What specific aspects of the presentation are causing you the most concern?",
                    isFromUser = false,
                    timestamp = Date(System.currentTimeMillis() - 3500000)
                )
            ),
            isLoading = false,
            error = null,
            navController = fakeNavController
        )
    }
}

@Preview(name = "Conversation Detail - Loading", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_ConversationDetailScreen_Loading() {
    val fakeNavController = androidx.navigation.compose.rememberNavController()

    OrielleThemeWithPreference(themeManager = com.orielle.ui.theme.ThemeManager(androidx.compose.ui.platform.LocalContext.current)) {
        ConversationDetailContent(
            conversation = null,
            messages = emptyList(),
            isLoading = true,
            error = null,
            navController = fakeNavController
        )
    }
}

@Preview(name = "Conversation Detail - Error", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_ConversationDetailScreen_Error() {
    val fakeNavController = androidx.navigation.compose.rememberNavController()

    OrielleThemeWithPreference(themeManager = com.orielle.ui.theme.ThemeManager(androidx.compose.ui.platform.LocalContext.current)) {
        ConversationDetailContent(
            conversation = null,
            messages = emptyList(),
            isLoading = false,
            error = "Failed to load conversation: Network error",
            navController = fakeNavController
        )
    }
}
