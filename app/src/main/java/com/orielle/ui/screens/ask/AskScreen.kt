package com.orielle.ui.screens.ask

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
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
import kotlinx.coroutines.delay

data class ChatMessage(
    val id: String,
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskScreen(
    navController: NavController,
    viewModel: AskViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = MaterialTheme.colorScheme.background == DarkGray
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    var showPrivacyCoachMark by remember { mutableStateOf(false) }
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

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

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            // Header
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.messages.size > 1) { // More than just welcome message
                            viewModel.showChoiceModal()
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showPrivacyCoachMark = true }) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Privacy",
                                tint = textColor
                            )
                        }

                        // Privacy coach mark
                        if (showPrivacyCoachMark) {
                            PrivacyCoachMark(
                                onDismiss = {
                                    showPrivacyCoachMark = false
                                    viewModel.markPrivacyCoachMarkSeen()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Chat messages
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(uiState.messages) { message ->
                    ChatBubble(
                        message = message,
                        isDark = isDark
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

        // Choice modal overlay
        if (uiState.showChoiceModal) {
            ChoiceModal(
                onSaveConversation = {
                    viewModel.hideChoiceModal()
                    navController.navigate("ask_tagging")
                },
                onLetItGo = {
                    viewModel.letItGo()
                    navController.popBackStack()
                    // TODO: Show toast "Your thoughts have been released."
                },
                isDark = isDark
            )
        }
    }
}

@Composable
fun ChatBubble(
    message: ChatMessage,
    isDark: Boolean
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
                    .size(28.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            modifier = Modifier
                .widthIn(max = 280.dp),
            shape = RoundedCornerShape(16.dp),
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
                text = message.text,
                modifier = Modifier.padding(16.dp),
                style = Typography.bodyLarge,
                color = if (message.isFromUser) {
                    Color.White
                } else {
                    if (isDark) SoftSand else Charcoal
                }
            )
        }

        if (message.isFromUser) {
            Spacer(modifier = Modifier.width(16.dp))
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
                .size(28.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))

        Card(
            modifier = Modifier.widthIn(max = 120.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) DarkGray else SoftSand
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isDark) 0.dp else 4.dp
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
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
            .size(6.dp)
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
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) DarkGray else SoftSand
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isDark) 0.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = messageText,
                onValueChange = onMessageTextChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Ask Orielle anything...",
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

            Spacer(modifier = Modifier.width(8.dp))

            // Mic/Send button with animation
            Box(
                modifier = Modifier.size(40.dp),
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
                            imageVector = Icons.Default.Send,
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
        Card(
            modifier = Modifier
                .padding(top = 60.dp, end = 16.dp)
                .widthIn(max = 280.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = WaterBlue
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Text(
                text = "This lock is our promise. Your reflections here are always private and secure.",
                modifier = Modifier.padding(16.dp),
                style = Typography.bodyMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
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
                text = "I feel so scattered and anxious today.",
                isFromUser = true
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
                text = "I hear you. Can you tell me more about what you're feeling right now?",
                isFromUser = false
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
                text = "I feel so scattered and anxious today.",
                isFromUser = true
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
                text = "I hear you. Can you tell me more about what you're feeling right now?",
                isFromUser = false
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
