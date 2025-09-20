package com.orielle.ui.screens.ask

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.orielle.R
import com.orielle.ui.theme.*
import com.orielle.domain.model.ChatMessage
import com.orielle.domain.manager.SessionManager
import com.orielle.ui.util.ScreenUtils
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.hilt.navigation.compose.hiltViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskScreen(
    navController: NavController,
    viewModel: AskViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = MaterialTheme.colorScheme.background == DarkGray
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    var showPrivacyCoachMark by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // User profile info for chat bubbles (simplified for now)
    var userProfileImageUrl by remember { mutableStateOf<String?>(null) }
    var userName by remember { mutableStateOf<String?>(null) }

    // Show privacy coach mark on first use
    LaunchedEffect(Unit) {
        if (viewModel.isFirstTimeUser()) {
            showPrivacyCoachMark = true
        }
    }

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            // Header - Minimal spacing
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (uiState.messages.size > 1) { // More than just welcome message
                                viewModel.showChoiceModal()
                            } else {
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier.size(ScreenUtils.responsiveIconSize(48.dp))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor,
                            modifier = Modifier.size(ScreenUtils.responsiveIconSize(24.dp))
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showPrivacyCoachMark = true },
                        modifier = Modifier.size(ScreenUtils.responsiveIconSize(48.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Privacy",
                            tint = textColor,
                            modifier = Modifier.size(ScreenUtils.responsiveIconSize(24.dp))
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                ),
                modifier = Modifier.padding(horizontal = ScreenUtils.responsiveSpacing())
            )

            // Chat messages - Minimal top spacing
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = ScreenUtils.responsivePadding()),
                verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding() * 1.25f),
                contentPadding = PaddingValues(top = ScreenUtils.responsiveTextSpacing(), bottom = ScreenUtils.responsivePadding() * 1.25f)
            ) {
                items(uiState.messages) { message ->
                    ChatBubble(
                        message = message,
                        isDark = isDark,
                        userProfileImageUrl = userProfileImageUrl,
                        userName = userName
                    )
                }

                // Typing indicator
                if (uiState.isTyping) {
                    item {
                        TypingIndicator(isDark = isDark)
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
                isDark = isDark,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Privacy coach mark overlay - now a proper chat bubble
        if (showPrivacyCoachMark) {
            PrivacyCoachMark(
                onDismiss = {
                    showPrivacyCoachMark = false
                    viewModel.markPrivacyCoachMarkSeen()
                },
                isDark = isDark
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
                },
                isDark = isDark
            )
        }

        // Tagging modal
        if (uiState.showTaggingModal) {
            val cardColor = if (isDark) Color(0xFF2A2A2A) else Color.White
            TaggingModal(
                currentTags = uiState.currentTags,
                onDismiss = { viewModel.hideTaggingModal() },
                onSaveTags = { tags ->
                    viewModel.addTags(tags)
                    viewModel.hideTaggingModal()
                    navController.popBackStack()
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
    isDark: Boolean,
    userProfileImageUrl: String? = null,
    userName: String? = null
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
                    if (isDark) DarkGray else SoftSand
                }
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isDark) 0.dp else 4.dp
            )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(ScreenUtils.responsivePadding()),
                style = Typography.bodyLarge,
                color = if (message.isFromUser) {
                    Color.White
                } else {
                    if (isDark) SoftSand else Charcoal
                }
            )
        }

        if (message.isFromUser) {
            Spacer(modifier = Modifier.width(ScreenUtils.responsiveSpacing() * 1.5f))
            // User's avatar or initial
            if (userProfileImageUrl != null && userProfileImageUrl.isNotBlank()) {
                // Show user's actual profile image
                AsyncImage(
                    model = userProfileImageUrl,
                    contentDescription = "You",
                    modifier = Modifier
                        .size(ScreenUtils.responsiveIconSize(32.dp))
                        .clip(CircleShape),
                    placeholder = painterResource(id = R.drawable.ic_orielle_drop),
                    error = painterResource(id = R.drawable.ic_orielle_drop)
                )
            } else if (!userName.isNullOrBlank()) {
                // Show user's initial in a circle
                Box(
                    modifier = Modifier
                        .size(ScreenUtils.responsiveIconSize(32.dp))
                        .background(
                            color = if (isDark) DarkGray else SoftSand,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.first().uppercase(),
                        style = Typography.titleMedium,
                        color = if (isDark) SoftSand else Charcoal,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                // Default chat bubble - no avatar
                // This creates a more natural chat appearance
            }
        }
    }
}

@Composable
fun TypingIndicator(isDark: Boolean) {
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
                containerColor = if (isDark) DarkGray else SoftSand
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isDark) 0.dp else 4.dp
            )
        ) {
            Row(
                modifier = Modifier.padding(ScreenUtils.responsivePadding()),
                horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveTextSpacing())
            ) {
                repeat(3) { index ->
                    TypingDot(
                        delay = index * 200L,
                        color = if (isDark) SoftSand else Charcoal
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
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val hasText = messageText.isNotBlank()

    Card(
        modifier = modifier.padding(horizontal = ScreenUtils.responsivePadding(), vertical = ScreenUtils.responsivePadding() * 1.25f),
        shape = RoundedCornerShape(ScreenUtils.responsivePadding() * 1.5f),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) DarkGray else SoftSand
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDark) 0.dp else 4.dp
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
                        color = if (isDark) SoftSand.copy(alpha = 0.6f) else Charcoal.copy(alpha = 0.6f)
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = if (isDark) SoftSand else Charcoal,
                    unfocusedTextColor = if (isDark) SoftSand else Charcoal
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
                            tint = if (isDark) SoftSand else Charcoal
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
fun PrivacyCoachMark(onDismiss: () -> Unit, isDark: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onDismiss() },
        contentAlignment = Alignment.TopEnd
    ) {
        // Chat bubble pointing to the lock icon
        Card(
            modifier = Modifier
                .padding(top = 80.dp, end = 16.dp)
                .widthIn(max = 280.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) DarkGray else SoftSand
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Chat bubble tail pointing to lock icon
                Box(
                    modifier = Modifier
                        .align(Alignment.End)
                        .offset(y = (-8).dp)
                        .size(16.dp)
                        .background(
                            color = if (isDark) DarkGray else SoftSand,
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 0.dp, bottomEnd = 0.dp)
                        )
                )

                Text(
                    text = "This lock is our promise. Your reflections here are always private and secure.",
                    style = Typography.bodyMedium,
                    color = if (isDark) SoftSand else Charcoal,
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
    isDark: Boolean
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
                containerColor = if (isDark) DarkGray else SoftSand
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isDark) 0.dp else 8.dp
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
                    color = if (isDark) SoftSand else Charcoal,
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
                        color = if (isDark) SoftSand else Charcoal
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
    OrielleTheme(darkTheme = false) {
        AskScreen(navController = fakeNavController)
    }
}

@Preview(name = "Ask Screen - Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun Preview_AskScreen_Dark() {
    val fakeNavController = rememberNavController()
    OrielleTheme(darkTheme = true) {
        AskScreen(navController = fakeNavController)
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
            isDark = false
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
            isDark = false
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
            isDark = true
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
            isDark = true
        )
    }
}

@Preview(name = "Typing Indicator - Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_TypingIndicator_Light() {
    OrielleTheme(darkTheme = false) {
        TypingIndicator(isDark = false)
    }
}

@Preview(name = "Typing Indicator - Dark", showBackground = true, backgroundColor = 0xFF1A1A1A)
@Composable
fun Preview_TypingIndicator_Dark() {
    OrielleTheme(darkTheme = true) {
        TypingIndicator(isDark = true)
    }
}

@Preview(name = "Text Input Bar - Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_TextInputBar_Light() {
    OrielleTheme(darkTheme = false) {
        TextInputBar(
            messageText = "",
            onMessageTextChange = {},
            onSendMessage = {},
            isDark = false
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
            onSendMessage = {},
            isDark = false
        )
    }
}

@Preview(name = "Choice Modal - Light", showBackground = true, backgroundColor = 0xFFF6F5F1)
@Composable
fun Preview_ChoiceModal_Light() {
    OrielleTheme(darkTheme = false) {
        ChoiceModal(
            onSaveConversation = {},
            onLetItGo = {},
            isDark = false
        )
    }
}

@Composable
private fun TaggingModal(
    currentTags: List<String>,
    onDismiss: () -> Unit,
    onSaveTags: (List<String>) -> Unit,
    textColor: Color,
    cardColor: Color
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

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                    contentPadding = PaddingValues(vertical = 4.dp)
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
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = Typography.bodySmall.copy(
                                    color = if (isSelected) Color.White else textColor,
                                    fontWeight = FontWeight.Medium
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

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing()),
                        contentPadding = PaddingValues(vertical = 4.dp)
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
                                            fontWeight = FontWeight.Medium
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
            onLetItGo = {},
            isDark = true
        )
    }
}
