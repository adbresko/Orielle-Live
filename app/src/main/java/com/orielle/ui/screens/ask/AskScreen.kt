package com.orielle.ui.screens.ask

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.orielle.R
import com.orielle.ui.theme.*
import com.orielle.domain.model.ChatMessage
import com.orielle.domain.manager.SessionManager
import com.orielle.ui.components.AvatarOption
import com.orielle.ui.components.hexToColor
import com.orielle.ui.util.ScreenUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import androidx.core.graphics.toColorInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskScreen(
    navController: NavController,
    sessionManager: SessionManager,
    viewModel: AskViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    var showPrivacyCoachMark by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // User profile info for chat bubbles - get from session manager
    var userProfileImageUrl by remember { mutableStateOf<String?>(null) }
    var userLocalImagePath by remember { mutableStateOf<String?>(null) }
    var userSelectedAvatarId by remember { mutableStateOf<String?>(null) }
    var userBackgroundColorHex by remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf<String?>(null) }

    // Load user profile data from cached profile
    LaunchedEffect(Unit) {
        try {
            val userId = sessionManager.currentUserId.first()
            if (userId != null && !sessionManager.isGuest.first()) {
                val cachedProfile = sessionManager.getCachedUserProfile(userId)
                if (cachedProfile != null) {
                    userName = cachedProfile.firstName ?: cachedProfile.displayName ?: "User"
                    userProfileImageUrl = cachedProfile.profileImageUrl
                    userLocalImagePath = cachedProfile.localImagePath
                    userSelectedAvatarId = cachedProfile.selectedAvatarId
                    userBackgroundColorHex = cachedProfile.backgroundColorHex
                    Timber.d("📋 AskScreen: Loaded cached profile data for user: $userId")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "❌ AskScreen: Failed to load cached user profile")
            userName = "User" // Fallback
        }
    }

    // Privacy coach mark only shows when lock is clicked

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ScreenUtils.responsivePadding()),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side - Back arrow icon
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(ScreenUtils.responsiveIconSize(28.dp))
                        .clickable {
                            if (uiState.messages.size > 1) { // More than just welcome message
                                viewModel.showChoiceModal()
                            } else {
                                navController.popBackStack()
                            }
                        },
                    tint = textColor
                )

                Spacer(Modifier.weight(1f))

                // Right side - Tag icon (only show if conversation has messages)
                if (uiState.messages.size > 1) {
                    Icon(
                        imageVector = Icons.Default.Tag,
                        contentDescription = "Add Tags",
                        modifier = Modifier
                            .size(ScreenUtils.responsiveIconSize(28.dp))
                            .clickable { viewModel.showTaggingModal() },
                        tint = if (uiState.currentTags.isNotEmpty()) StillwaterTeal else textColor
                    )
                    Spacer(modifier = Modifier.width(ScreenUtils.responsiveSpacing()))
                }

                // Lock icon
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Privacy",
                    modifier = Modifier
                        .size(ScreenUtils.responsiveIconSize(28.dp))
                        .clickable { showPrivacyCoachMark = true },
                    tint = textColor
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            // Subtle line separator between top bar and content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(WaterBlue.copy(alpha = 0.3f))
            )

            // Chat messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = ScreenUtils.responsivePadding()),
                verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding() * 1.25f),
                contentPadding = PaddingValues(top = ScreenUtils.responsivePadding() * 1.5f, bottom = ScreenUtils.responsivePadding() * 1.25f)
            ) {
                items(uiState.messages) { message ->
                    ChatBubble(
                        message = message,
                        userProfileImageUrl = userProfileImageUrl,
                        userLocalImagePath = userLocalImagePath,
                        userSelectedAvatarId = userSelectedAvatarId,
                        userBackgroundColorHex = userBackgroundColorHex,
                        userName = userName
                    )
                }

                // Typing indicator
                if (uiState.isTyping) {
                    item {
                        TypingIndicator()
                    }
                }
            }

            // Text input
            TextInputBar(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendMessage = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText)
                        messageText = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Privacy coach mark overlay - shows only when lock is clicked
        if (showPrivacyCoachMark) {
            PrivacyCoachMark(
                onDismiss = {
                    showPrivacyCoachMark = false
                }
            )
        }

        // Choice modal overlay
        if (uiState.showChoiceModal) {
            ChoiceModal(
                onSaveConversation = {
                    viewModel.hideChoiceModal()
                    viewModel.showTaggingModal()
                },
                onLetItGo = {
                    viewModel.letItGo()
                    navController.popBackStack()
                    // TODO: Show toast "Your thoughts have been released."
                }
            )
        }

        // Title and Tagging modal
        if (uiState.showTaggingModal) {
            val cardColor = MaterialTheme.colorScheme.surface
            TitleAndTaggingModal(
                currentTitle = uiState.conversationTitle,
                suggestedTitle = uiState.suggestedTitle,
                currentTags = uiState.currentTags,
                onDismiss = { viewModel.hideTaggingModal() },
                onSaveTitleAndTags = { title, tags ->
                    viewModel.updateConversationTitle(title)
                    viewModel.addTags(tags)
                    viewModel.hideTaggingModal()
                    // Don't close the chat - just hide the modal and continue the conversation
                },
                textColor = textColor,
                cardColor = cardColor
            )
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    userProfileImageUrl: String? = null,
    userLocalImagePath: String? = null,
    userSelectedAvatarId: String? = null,
    userBackgroundColorHex: String? = null,
    userName: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!message.isFromUser) {
            // Orielle's avatar
            Image(
                painter = painterResource(id = R.drawable.ic_orielle_drop),
                contentDescription = "Orielle",
                modifier = Modifier
                    .size(ScreenUtils.responsiveIconSize(32.dp))
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(ScreenUtils.responsiveSpacing() * 1.5f))
        }

        Card(
            modifier = Modifier
                .widthIn(max = ScreenUtils.responsivePadding() * 17.5f),
            shape = RoundedCornerShape(ScreenUtils.responsivePadding()),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) {
                    WaterBlue
                } else {
                    MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(ScreenUtils.responsivePadding()),
                style = Typography.bodyLarge,
                color = if (message.isFromUser) {
                    Color.White
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }

        if (message.isFromUser) {
            Spacer(modifier = Modifier.width(ScreenUtils.responsiveSpacing() * 1.5f))
            // User's avatar - priority: local image > remote image > mood icon > initial
            UserAvatar(
                userProfileImageUrl = userProfileImageUrl,
                userLocalImagePath = userLocalImagePath,
                userSelectedAvatarId = userSelectedAvatarId,
                userBackgroundColorHex = userBackgroundColorHex,
                userName = userName
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        // Orielle's avatar
        Image(
            painter = painterResource(id = R.drawable.ic_orielle_drop),
            contentDescription = "Orielle",
            modifier = Modifier
                .size(ScreenUtils.responsiveIconSize(32.dp))
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(ScreenUtils.responsiveSpacing() * 1.5f))

        Card(
            modifier = Modifier.widthIn(max = ScreenUtils.responsivePadding() * 7.5f),
            shape = RoundedCornerShape(ScreenUtils.responsivePadding()),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Row(
                modifier = Modifier.padding(ScreenUtils.responsivePadding()),
                horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveTextSpacing())
            ) {
                repeat(3) { index ->
                    TypingDot(
                        delay = index * 200L,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun TypingDot(delay: Long, color: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, delayMillis = delay.toInt()),
            repeatMode = RepeatMode.Reverse
        ), label = "alpha"
    )

    Box(
        modifier = Modifier
            .size(ScreenUtils.responsiveIconSize(6.dp))
            .background(
                color = color.copy(alpha = alpha),
                shape = CircleShape
            )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextInputBar(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasText = messageText.isNotBlank()

    Card(
        modifier = modifier.padding(horizontal = ScreenUtils.responsivePadding(), vertical = ScreenUtils.responsivePadding() * 1.25f),
        shape = RoundedCornerShape(ScreenUtils.responsivePadding() * 1.5f),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = ScreenUtils.responsivePadding(), vertical = ScreenUtils.responsiveSpacing()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "What's on your mind?",
                        style = Typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(
                    onSend = { onSendMessage() }
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(ScreenUtils.responsiveSpacing()))

            // Mic/Send button with animation
            Box(
                modifier = Modifier.size(ScreenUtils.responsiveIconSize(40.dp)),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.animation.AnimatedVisibility(
                    visible = !hasText,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(
                        onClick = { /* TODO: Implement voice recording */ },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Voice message",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = hasText,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    IconButton(
                        onClick = onSendMessage,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message",
                            tint = WaterBlue
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyCoachMark(onDismiss: () -> Unit) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss() },
        contentAlignment = Alignment.TopEnd
    ) {
        // Clean privacy message card
        Card(
            modifier = Modifier
                .padding(top = 80.dp, end = 16.dp)
                .widthIn(max = 280.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Privacy Promise",
                    style = Typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "This lock is our promise. Your reflections here are always private and secure.",
                    style = Typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun ChoiceModal(
    onSaveConversation: () -> Unit,
    onLetItGo: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable { /* Prevent dismissal on background click */ },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "How would you like to end this session?",
                    style = Typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Save conversation button
                Button(
                    onClick = onSaveConversation,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StillwaterTeal
                    )
                ) {
                    Text(
                        text = "Save conversation",
                        style = Typography.labelLarge,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // Let it go button
                TextButton(
                    onClick = onLetItGo,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Let it go for now",
                        style = Typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Preview(name = "Ask Screen - Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_AskScreen_Light() {
    val fakeNavController = rememberNavController()
    // Create a mock SessionManager for preview
    val mockSessionManager = object : SessionManager {
        override val currentUserId = kotlinx.coroutines.flow.flowOf("preview_user")
        override val isGuest = kotlinx.coroutines.flow.flowOf(false)
        override val hasSeenOnboarding = kotlinx.coroutines.flow.flowOf(true)
        override suspend fun setHasSeenOnboarding(seen: Boolean) {}
        override suspend fun startGuestSession() {}
        override suspend fun endGuestSession() {}
        override suspend fun getLastCheckInTimestamp(): Long? = null
        override suspend fun setLastCheckInTimestamp(timestamp: Long) {}
        override suspend fun clearLastCheckInTimestamp() {}
        override suspend fun clearSession() {}
        override suspend fun cacheUserProfile(userId: String, firstName: String?, displayName: String?, email: String?, profileImageUrl: String?, localImagePath: String?, selectedAvatarId: String?, backgroundColorHex: String?, isPremium: Boolean, notificationsEnabled: Boolean, twoFactorEnabled: Boolean) {}
        override suspend fun getCachedUserProfile(userId: String) = null
        override suspend fun updateCachedUserProfile(userId: String, firstName: String?, displayName: String?, email: String?, profileImageUrl: String?, localImagePath: String?, selectedAvatarId: String?, backgroundColorHex: String?, isPremium: Boolean?, notificationsEnabled: Boolean?, twoFactorEnabled: Boolean?) {}
        override suspend fun clearCachedUserProfile(userId: String) {}
        override suspend fun hasValidCachedProfile(userId: String): Boolean = false
        override suspend fun getProfileCacheExpiration(): Long = 3600000L
        override suspend fun cacheMoodCheckInCompleted(userId: String, date: String) {}
        override suspend fun hasCachedMoodCheckInToday(userId: String): Boolean? = null
        override suspend fun clearCachedMoodCheckIn(userId: String) {}
    }

    OrielleTheme(darkTheme = false) {
        AskScreen(
            navController = fakeNavController,
            sessionManager = mockSessionManager
        )
    }
}

@Preview(name = "Ask Screen - Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun Preview_AskScreen_Dark() {
    val fakeNavController = rememberNavController()
    // Create a mock SessionManager for preview
    val mockSessionManager = object : SessionManager {
        override val currentUserId = kotlinx.coroutines.flow.flowOf("preview_user")
        override val isGuest = kotlinx.coroutines.flow.flowOf(false)
        override val hasSeenOnboarding = kotlinx.coroutines.flow.flowOf(true)
        override suspend fun setHasSeenOnboarding(seen: Boolean) {}
        override suspend fun startGuestSession() {}
        override suspend fun endGuestSession() {}
        override suspend fun getLastCheckInTimestamp(): Long? = null
        override suspend fun setLastCheckInTimestamp(timestamp: Long) {}
        override suspend fun clearLastCheckInTimestamp() {}
        override suspend fun clearSession() {}
        override suspend fun cacheUserProfile(userId: String, firstName: String?, displayName: String?, email: String?, profileImageUrl: String?, localImagePath: String?, selectedAvatarId: String?, backgroundColorHex: String?, isPremium: Boolean, notificationsEnabled: Boolean, twoFactorEnabled: Boolean) {}
        override suspend fun getCachedUserProfile(userId: String) = null
        override suspend fun updateCachedUserProfile(userId: String, firstName: String?, displayName: String?, email: String?, profileImageUrl: String?, localImagePath: String?, selectedAvatarId: String?, backgroundColorHex: String?, isPremium: Boolean?, notificationsEnabled: Boolean?, twoFactorEnabled: Boolean?) {}
        override suspend fun clearCachedUserProfile(userId: String) {}
        override suspend fun hasValidCachedProfile(userId: String): Boolean = false
        override suspend fun getProfileCacheExpiration(): Long = 3600000L
        override suspend fun cacheMoodCheckInCompleted(userId: String, date: String) {}
        override suspend fun hasCachedMoodCheckInToday(userId: String): Boolean? = null
        override suspend fun clearCachedMoodCheckIn(userId: String) {}
    }

    OrielleTheme(darkTheme = true) {
        AskScreen(
            navController = fakeNavController,
            sessionManager = mockSessionManager
        )
    }
}

@Preview(name = "Chat Bubble - User Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_ChatBubble_User_Light() {
    OrielleTheme(darkTheme = false) {
        ChatBubble(
            message = ChatMessage(
                id = "1",
                conversationId = "preview",
                content = "I feel so scattered and anxious today.",
                isFromUser = true,
                timestamp = java.util.Date()
            ),
        )
    }
}

@Preview(name = "Chat Bubble - Orielle Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_ChatBubble_Orielle_Light() {
    OrielleTheme(darkTheme = false) {
        ChatBubble(
            message = ChatMessage(
                id = "2",
                conversationId = "preview",
                content = "I hear you. Can you tell me more about what you're feeling right now?",
                isFromUser = false,
                timestamp = java.util.Date()
            ),
        )
    }
}

@Preview(name = "Chat Bubble - User Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun Preview_ChatBubble_User_Dark() {
    OrielleTheme(darkTheme = true) {
        ChatBubble(
            message = ChatMessage(
                id = "1",
                conversationId = "preview",
                content = "I feel so scattered and anxious today.",
                isFromUser = true,
                timestamp = java.util.Date()
            ),
        )
    }
}

@Preview(name = "Chat Bubble - Orielle Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun Preview_ChatBubble_Orielle_Dark() {
    OrielleTheme(darkTheme = true) {
        ChatBubble(
            message = ChatMessage(
                id = "2",
                conversationId = "preview",
                content = "I hear you. Can you tell me more about what you're feeling right now?",
                isFromUser = false,
                timestamp = java.util.Date()
            ),
        )
    }
}

@Preview(name = "Typing Indicator - Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_TypingIndicator_Light() {
    OrielleTheme(darkTheme = false) {
        TypingIndicator()
    }
}

@Preview(name = "Typing Indicator - Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun Preview_TypingIndicator_Dark() {
    OrielleTheme(darkTheme = true) {
        TypingIndicator()
    }
}

@Preview(name = "Text Input Bar - Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_TextInputBar_Light() {
    OrielleTheme(darkTheme = false) {
        TextInputBar(
            messageText = "",
            onMessageTextChange = {},
            onSendMessage = {}
        )
    }
}

@Preview(name = "Text Input Bar with Text - Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_TextInputBar_WithText_Light() {
    OrielleTheme(darkTheme = false) {
        TextInputBar(
            messageText = "Hello Orielle",
            onMessageTextChange = {},
            onSendMessage = {}
        )
    }
}

@Preview(name = "Choice Modal - Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_ChoiceModal_Light() {
    OrielleTheme(darkTheme = false) {
        ChoiceModal(
            onSaveConversation = {},
            onLetItGo = {}
        )
    }
}

@Composable
private fun TitleAndTaggingModal(
    currentTitle: String,
    suggestedTitle: String,
    currentTags: List<String>,
    onDismiss: () -> Unit,
    onSaveTitleAndTags: (String, List<String>) -> Unit,
    textColor: Color,
    cardColor: Color,
) {
    var titleInput by remember { mutableStateOf(currentTitle) }
    var selectedTags by remember { mutableStateOf(currentTags.toSet()) }
    var customTagInput by remember { mutableStateOf("") }

    val suggestedTags = listOf(
        "gratitude", "reflection", "growth", "challenge", "joy",
        "anxiety", "peace", "love", "work", "family", "friends",
        "health", "goals", "dreams", "memories", "learning"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Title & Tags",
                style = Typography.titleLarge.copy(
                    color = textColor,
                    fontSize = (20 * ScreenUtils.getTextScaleFactor()).sp
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding())
            ) {
                // Title input section
                Text(
                    text = "Conversation Title:",
                    style = Typography.bodyMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp
                    )
                )

                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    placeholder = {
                        Text(
                            text = suggestedTitle.ifEmpty { "Enter a title..." },
                            color = textColor.copy(alpha = 0.5f),
                            fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StillwaterTeal,
                        unfocusedBorderColor = textColor.copy(alpha = 0.3f),
                        cursorColor = StillwaterTeal
                    ),
                    textStyle = Typography.bodyMedium.copy(
                        color = textColor,
                        fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp
                    ),
                    singleLine = true
                )

                // Suggested title hint
                if (suggestedTitle.isNotEmpty() && titleInput.isEmpty()) {
                    Text(
                        text = "Suggested: \"$suggestedTitle\"",
                        style = Typography.bodySmall.copy(
                            color = textColor.copy(alpha = 0.7f),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            fontSize = (14 * ScreenUtils.getTextScaleFactor()).sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing()))

                // Custom tag input
                Text(
                    text = "Add Tags:",
                    style = Typography.bodyMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.Medium,
                        fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp
                    )
                )

                OutlinedTextField(
                    value = customTagInput,
                    onValueChange = { customTagInput = it },
                    placeholder = {
                        Text(
                            text = "Add custom tag...",
                            color = textColor.copy(alpha = 0.5f),
                            fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StillwaterTeal,
                        unfocusedBorderColor = textColor.copy(alpha = 0.3f),
                        cursorColor = StillwaterTeal
                    ),
                    textStyle = Typography.bodyMedium.copy(
                        color = textColor,
                        fontSize = (16 * ScreenUtils.getTextScaleFactor()).sp
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (customTagInput.trim().isNotEmpty()) {
                                selectedTags = selectedTags + customTagInput.trim()
                                customTagInput = ""
                            }
                        }
                    ),
                    singleLine = true
                )

                // Suggested tags
                Text(
                    text = "Suggested Tags:",
                    style = Typography.bodyMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.Medium
                    )
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                    verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.heightIn(max = ScreenUtils.responsiveImageSize(180.dp))
                ) {
                    items(suggestedTags) { tag ->
                        val isSelected = selectedTags.contains(tag)
                        Card(
                            modifier = Modifier.clickable {
                                selectedTags = if (isSelected) {
                                    selectedTags - tag
                                } else {
                                    selectedTags + tag
                                }
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) StillwaterTeal else cardColor
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(
                                    horizontal = ScreenUtils.responsiveSpacing() * 0.75f,
                                    vertical = ScreenUtils.responsiveSpacing() * 0.5f
                                ),
                                style = Typography.bodySmall.copy(
                                    color = if (isSelected) Color.White else textColor,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = (12 * ScreenUtils.getTextScaleFactor()).sp
                                )
                            )
                        }
                    }
                }

                // Selected tags display
                if (selectedTags.isNotEmpty()) {
                    Text(
                        text = "Selected Tags:",
                        style = Typography.bodyMedium.copy(
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                        verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier.heightIn(max = ScreenUtils.responsiveImageSize(120.dp))
                    ) {
                        items(selectedTags.toList()) { tag ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = StillwaterTeal.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        style = Typography.bodySmall.copy(
                                            color = StillwaterTeal,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = (12 * ScreenUtils.getTextScaleFactor()).sp
                                        )
                                    )
                                    IconButton(
                                        onClick = { selectedTags = selectedTags - tag },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove tag",
                                            tint = StillwaterTeal,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveTitleAndTags(
                        titleInput.ifEmpty { suggestedTitle },
                        selectedTags.toList()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = StillwaterTeal)
            ) {
                Text("Save", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = textColor)
            }
        }
    )
}

@Composable
private fun TaggingModal(
    currentTags: List<String>,
    onDismiss: () -> Unit,
    onSaveTags: (List<String>) -> Unit,
    textColor: Color,
    cardColor: Color,
) {
    var selectedTags by remember { mutableStateOf(currentTags.toSet()) }
    var customTagInput by remember { mutableStateOf("") }

    val suggestedTags = listOf(
        "gratitude", "reflection", "growth", "challenge", "joy",
        "anxiety", "peace", "love", "work", "family", "friends",
        "health", "goals", "dreams", "memories", "learning"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Tags",
                style = Typography.titleLarge.copy(color = textColor)
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding())
            ) {
                // Custom tag input
                OutlinedTextField(
                    value = customTagInput,
                    onValueChange = { customTagInput = it },
                    placeholder = {
                        Text(
                            text = "Add custom tag...",
                            color = textColor.copy(alpha = 0.5f)
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = StillwaterTeal,
                        unfocusedBorderColor = textColor.copy(alpha = 0.3f),
                        cursorColor = StillwaterTeal
                    ),
                    textStyle = Typography.bodyMedium.copy(color = textColor),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (customTagInput.trim().isNotEmpty()) {
                                selectedTags = selectedTags + customTagInput.trim()
                                customTagInput = ""
                            }
                        }
                    ),
                    singleLine = true
                )

                // Suggested tags
                Text(
                    text = "Suggested Tags:",
                    style = Typography.bodyMedium.copy(
                        color = textColor,
                        fontWeight = FontWeight.Medium
                    )
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                    verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                    contentPadding = PaddingValues(vertical = 4.dp),
                    modifier = Modifier.heightIn(max = ScreenUtils.responsiveImageSize(180.dp))
                ) {
                    items(suggestedTags) { tag ->
                        val isSelected = selectedTags.contains(tag)
                        Card(
                            modifier = Modifier.clickable {
                                selectedTags = if (isSelected) {
                                    selectedTags - tag
                                } else {
                                    selectedTags + tag
                                }
                            },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) StillwaterTeal else cardColor
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = tag,
                                modifier = Modifier.padding(
                                    horizontal = ScreenUtils.responsiveSpacing() * 0.75f,
                                    vertical = ScreenUtils.responsiveSpacing() * 0.5f
                                ),
                                style = Typography.bodySmall.copy(
                                    color = if (isSelected) Color.White else textColor,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = (12 * ScreenUtils.getTextScaleFactor()).sp
                                )
                            )
                        }
                    }
                }

                // Selected tags display
                if (selectedTags.isNotEmpty()) {
                    Text(
                        text = "Selected Tags:",
                        style = Typography.bodyMedium.copy(
                            color = textColor,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                        verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                        contentPadding = PaddingValues(vertical = 4.dp),
                        modifier = Modifier.heightIn(max = ScreenUtils.responsiveImageSize(120.dp))
                    ) {
                        items(selectedTags.toList()) { tag ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = StillwaterTeal.copy(alpha = 0.1f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = tag,
                                        style = Typography.bodySmall.copy(
                                            color = StillwaterTeal,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = (12 * ScreenUtils.getTextScaleFactor()).sp
                                        )
                                    )
                                    IconButton(
                                        onClick = { selectedTags = selectedTags - tag },
                                        modifier = Modifier.size(16.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove tag",
                                            tint = StillwaterTeal,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSaveTags(selectedTags.toList()) },
                colors = ButtonDefaults.buttonColors(containerColor = StillwaterTeal)
            ) {
                Text("Save Tags", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = textColor)
            }
        }
    )
}

@Preview(name = "Choice Modal - Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun Preview_ChoiceModal_Dark() {
    OrielleTheme(darkTheme = true) {
        ChoiceModal(
            onSaveConversation = {},
            onLetItGo = {}
        )
    }
}

@Preview(name = "Title and Tagging Modal - Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_TitleAndTaggingModal_Light() {
    OrielleTheme(darkTheme = false) {
        TitleAndTaggingModal(
            currentTitle = "Feeling anxious about work",
            suggestedTitle = "Work anxiety discussion",
            currentTags = listOf("anxiety", "work"),
            onDismiss = {},
            onSaveTitleAndTags = { _, _ -> },
            textColor = Charcoal,
            cardColor = Color.White
        )
    }
}

@Preview(name = "Title and Tagging Modal - Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun Preview_TitleAndTaggingModal_Dark() {
    OrielleTheme(darkTheme = true) {
        TitleAndTaggingModal(
            currentTitle = "",
            suggestedTitle = "Gratitude and reflection",
            currentTags = emptyList(),
            onDismiss = {},
            onSaveTitleAndTags = { _, _ -> },
            textColor = SoftSand,
            cardColor = Color(0xFF2A2A2A)
        )
    }
}

@Composable
private fun UserAvatar(
    userProfileImageUrl: String?,
    userLocalImagePath: String?,
    userSelectedAvatarId: String?,
    userBackgroundColorHex: String?,
    userName: String?,
) {
    android.util.Log.d("UserAvatar", "AskScreen UserAvatar - ImageUrl: $userProfileImageUrl, LocalPath: $userLocalImagePath, AvatarId: $userSelectedAvatarId, BackgroundColor: $userBackgroundColorHex, UserName: $userName")

    // Debug each condition
    android.util.Log.d("UserAvatar", "AskScreen Conditions:")
    android.util.Log.d("UserAvatar", "  - Local image: ${userLocalImagePath != null && File(userLocalImagePath).exists()}")
    android.util.Log.d("UserAvatar", "  - Remote image: ${userProfileImageUrl != null && userProfileImageUrl.isNotBlank() && userProfileImageUrl != "mood_icon"}")
    android.util.Log.d("UserAvatar", "  - Selected avatar: ${userSelectedAvatarId != null}")
    android.util.Log.d("UserAvatar", "  - Background color: ${userBackgroundColorHex != null}")
    android.util.Log.d("UserAvatar", "  - User name: ${!userName.isNullOrBlank()}")

    when {
        // Priority 1: Local uploaded image
        userLocalImagePath != null && File(userLocalImagePath).exists() -> {
            android.util.Log.d("UserAvatar", "AskScreen: Showing local image from $userLocalImagePath")
            Image(
                painter = rememberAsyncImagePainter(File(userLocalImagePath)),
                contentDescription = "Your Profile",
                modifier = Modifier
                    .size(ScreenUtils.responsiveIconSize(36.dp))
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        // Priority 2: Remote profile image (but not mood_icon)
        userProfileImageUrl != null && userProfileImageUrl.isNotBlank() && userProfileImageUrl != "mood_icon" -> {
            android.util.Log.d("UserAvatar", "AskScreen: Showing remote image from $userProfileImageUrl")
            AsyncImage(
                model = userProfileImageUrl,
                contentDescription = "Your Profile",
                modifier = Modifier
                    .size(ScreenUtils.responsiveIconSize(36.dp))
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.ic_orielle_drop),
                error = painterResource(id = R.drawable.ic_orielle_drop)
            )
        }

        // Priority 3: Selected avatar from library (mood icons)
        userSelectedAvatarId != null -> {
            android.util.Log.d("UserAvatar", "AskScreen: Showing selected avatar $userSelectedAvatarId")
            // Get mood icon resource directly
            val resourceId = getDrawableResourceId(userSelectedAvatarId)
            if (resourceId != null) {
                Image(
                    painter = painterResource(id = resourceId),
                    contentDescription = "Your Mood Avatar",
                    modifier = Modifier
                        .size(ScreenUtils.responsiveIconSize(36.dp))
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
            } else {
                android.util.Log.w("UserAvatar", "AskScreen: No resource found for avatar $userSelectedAvatarId, falling back to initials")
                // Fallback to colored background with initials
                UserInitialAvatarWithColor(userName = userName, backgroundColorHex = userBackgroundColorHex)
            }
        }

        // Priority 4: Background color with initials
        userBackgroundColorHex != null -> {
            android.util.Log.d("UserAvatar", "AskScreen: Showing background color with initials")
            UserInitialAvatarWithColor(userName = userName, backgroundColorHex = userBackgroundColorHex)
        }

        // Priority 5: User initial in themed circle
        !userName.isNullOrBlank() -> {
            UserInitialAvatar(userName = userName)
        }

        // Fallback: Use initials or default Orielle drop
        else -> {
            if (!userName.isNullOrBlank()) {
                UserInitialAvatar(userName = userName)
            } else {
                // Final fallback - Orielle drop icon
                Image(
                    painter = painterResource(id = R.drawable.ic_orielle_drop),
                    contentDescription = "Orielle",
                    modifier = Modifier.size(ScreenUtils.responsiveIconSize(36.dp)),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }
}

@Composable
private fun UserInitialAvatar(
    userName: String,
) {
    val avatarSize = ScreenUtils.responsiveIconSize(36.dp)

    Box(
        modifier = Modifier
            .size(avatarSize)
            .background(
                color = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = userName.first().uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun UserInitialAvatarWithColor(
    userName: String?,
    backgroundColorHex: String?,
) {
    val avatarSize = ScreenUtils.responsiveIconSize(36.dp)
    val initials = userName?.let { name ->
        name.trim().split(" ").firstOrNull()?.take(1)?.uppercase() ?: "U"
    } ?: "U"

    val themeColors = MaterialTheme.colorScheme

    // Parse background color from hex string, fallback to theme color
    val backgroundColor = remember(backgroundColorHex) {
        try {
            if (!backgroundColorHex.isNullOrBlank()) {
                val color = Color(backgroundColorHex.toColorInt())
                Timber.tag("InitialAvatarColor")
                    .d("Using background color: $backgroundColorHex -> $color")
                color
            } else {
                Timber.tag("InitialAvatarColor")
                    .d("No background color provided, using theme primary")
                themeColors.primary
            }
        } catch (e: Exception) {
            Timber.tag("InitialAvatarColor")
                .w(e, "Invalid background color hex: $backgroundColorHex, using theme color")
            themeColors.primary
        }
    }

    Box(
        modifier = Modifier
            .size(avatarSize)
            .background(
                color = backgroundColor,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

// Helper function to get drawable resource ID for mood icons
private fun getDrawableResourceId(avatarId: String): Int? {
    return when (avatarId) {
        "happy" -> R.drawable.ic_happy
        "playful" -> R.drawable.ic_playful
        "surprised" -> R.drawable.ic_surprised
        "peaceful" -> R.drawable.ic_peaceful
        "shy" -> R.drawable.ic_shy
        "sad" -> R.drawable.ic_sad
        "angry" -> R.drawable.ic_angry
        "frustrated" -> R.drawable.ic_frustrated
        "scared" -> R.drawable.ic_scared
        else -> null
    }
}
