package com.orielle.ui.screens.mood

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.animation.core.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.orielle.R
import com.orielle.data.local.model.QuoteEntity
import com.orielle.ui.components.OrielleScreenHeader
import com.orielle.ui.components.QuoteCardSkeleton
import com.orielle.ui.components.ErrorStateWithRetry
import com.orielle.ui.theme.*
import com.orielle.ui.util.ScreenUtils
import com.orielle.ui.util.ResponsivePreview

@Composable
fun GentleRewardScreen(
    mood: String,
    onDone: () -> Unit,
    viewModel: GentleRewardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Initialize with the selected mood
    LaunchedEffect(mood) {
        viewModel.loadQuoteForMood(mood)
    }

    GentleRewardScreenContent(
        mood = mood,
        quote = uiState.quote,
        isLoading = uiState.isLoading,
        error = uiState.error,
        isSharing = uiState.isSharing,
        onDone = onDone,
        onShare = { viewModel.shareQuote(context) },
        onRetry = { viewModel.loadQuoteForMood(mood) }
    )
}

@Composable
private fun GentleRewardScreenContent(
    mood: String,
    quote: QuoteEntity?,
    isLoading: Boolean,
    error: String?,
    isSharing: Boolean,
    onDone: () -> Unit,
    onShare: () -> Unit,
    onRetry: () -> Unit
) {
    // Get mood-specific background color
    val backgroundColor = getMoodBackgroundColor(mood)

    // Animate background color
    val animatedBackground by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 800),
        label = "background_fade"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor.copy(alpha = animatedBackground))
            .padding(ScreenUtils.responsivePadding() * 1.5f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Header
        OrielleScreenHeader(
            text = "Saved for today"
        )

        Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 2))

        // Quote Card with enhanced loading states
        when {
            isLoading -> {
                QuoteCardSkeleton()
            }
            quote != null -> {
                QuoteCard(
                    quote = quote,
                    onShare = onShare
                )
            }
            error != null -> {
                ErrorStateWithRetry(
                    error = error,
                    onRetry = onRetry
                )
            }
        }

        Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 3))

        // Action Buttons
        ActionButtons(
            onDone = onDone,
            onShare = if (quote != null) onShare else null,
            isSharing = isSharing
        )
    }
}

@Composable
private fun QuoteCard(
    quote: QuoteEntity,
    onShare: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(ScreenUtils.responsivePadding() * 2),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF5F5DC) // Parchment/aged paper color
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenUtils.responsivePadding())
            .clickable { onShare() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScreenUtils.responsivePadding() * 2)
        ) {
            // Pulsating blue glowing orb effect inside the card
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_scale"
            )
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.3f,
                targetValue = 0.7f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = EaseInOut),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulse_alpha"
            )

            // Circular orb background
            Box(
                modifier = Modifier
                    .size(160.dp * pulseScale)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF87CEEB).copy(alpha = pulseAlpha),
                                Color(0xFF87CEEB).copy(alpha = pulseAlpha * 0.6f),
                                Color(0xFF87CEEB).copy(alpha = pulseAlpha * 0.3f),
                                Color.Transparent
                            ),
                            radius = 80f
                        ),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
                    .align(Alignment.Center)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScreenUtils.responsivePadding()),
                horizontalAlignment = Alignment.Start // Left-aligned text
            ) {
                // Quote text (main focus, no mood indicator in the card)
                Text(
                    text = quote.quote,
                    fontSize = 24.sp,
                    fontFamily = FontFamily(androidx.compose.ui.text.font.Font(R.font.lora_bold)),
                    color = Color.Black,
                    textAlign = TextAlign.Start, // Left-aligned
                    fontWeight = FontWeight.Bold,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 1.5f))

                // Source (only show if author exists)
                if (quote.source.isNotBlank()) {
                    Text(
                        text = quote.source,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Black.copy(alpha = 0.7f),
                        textAlign = TextAlign.Start, // Left-aligned
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }
}


@Composable
private fun ErrorCard(
    error: String,
    onRetry: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(ScreenUtils.responsivePadding() * 2),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenUtils.responsivePadding()),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScreenUtils.responsivePadding() * 2),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Unable to load quote",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing()))

            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding()))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Try Again")
            }
        }
    }
}

@Composable
private fun ActionButtons(
    onDone: () -> Unit,
    onShare: (() -> Unit)?,
    isSharing: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing() * 2)
    ) {
        // Share link (above the Done button as in the design)
        if (onShare != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing())
            ) {
                Text(
                    text = "Share your insight",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onShare() }
                )
                if (isSharing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(ScreenUtils.responsiveIconSize(20.dp)),
                        color = StillwaterTeal,
                        strokeWidth = 2.dp
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.icon_share),
                        contentDescription = "Share",
                        modifier = Modifier
                            .size(ScreenUtils.responsiveIconSize(24.dp))
                            .clickable { onShare() }
                    )
                }
            }
        }

        // Done button
        Button(
            onClick = onDone,
            shape = RoundedCornerShape(ScreenUtils.responsivePadding() * 2),
            colors = ButtonDefaults.buttonColors(containerColor = StillwaterTeal),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = ScreenUtils.responsivePadding() * 2)
        ) {
            Text(
                text = "Done",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }
    }
}

@Composable
private fun getMoodBackgroundColor(mood: String): Color {
    return when (mood.lowercase()) {
        "happy" -> HappyBackground
        "sad" -> SadBackground
        "angry" -> AngryBackground
        "surprised" -> SurprisedBackground
        "playful" -> PlayfulBackground
        "shy" -> ShyBackground
        "frustrated" -> FrustratedBackground
        "scared" -> ScaredBackground
        "peaceful" -> PeacefulBackground
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
}

private fun getMoodIcon(mood: String): Int {
    return when (mood.lowercase()) {
        "happy" -> R.drawable.ic_happy
        "sad" -> R.drawable.ic_sad
        "angry" -> R.drawable.ic_angry
        "surprised" -> R.drawable.ic_surprised
        "playful" -> R.drawable.ic_playful
        "shy" -> R.drawable.ic_shy
        "frustrated" -> R.drawable.ic_frustrated
        "scared" -> R.drawable.ic_scared
        "peaceful" -> R.drawable.ic_peaceful
        else -> R.drawable.ic_happy
    }
}

@Preview(showBackground = true, name = "Gentle Reward - Happy")
@Composable
private fun GentleRewardScreenPreview() {
    OrielleTheme {
        GentleRewardScreenContent(
            mood = "Happy",
            quote = QuoteEntity(
                id = "happy_01",
                quote = "The best way to predict the future is to create it.",
                source = "Abraham Lincoln",
                mood = "Happy"
            ),
            isLoading = false,
            error = null,
            isSharing = false,
            onDone = {},
            onShare = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, name = "Gentle Reward - Dark")
@Composable
private fun GentleRewardScreenDarkPreview() {
    OrielleTheme {
        GentleRewardScreenContent(
            mood = "Sad",
            quote = QuoteEntity(
                id = "sad_01",
                quote = "Even in our sleep, pain which cannot be forgotten falls drop by drop upon the heart.",
                source = "Aeschylus",
                mood = "Sad"
            ),
            isLoading = false,
            error = null,
            isSharing = false,
            onDone = {},
            onShare = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Gentle Reward - Loading")
@Composable
private fun GentleRewardLoadingPreview() {
    OrielleTheme {
        GentleRewardScreenContent(
            mood = "Peaceful",
            quote = null,
            isLoading = true,
            error = null,
            isSharing = false,
            onDone = {},
            onShare = {},
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, name = "Gentle Reward - No Author")
@Composable
private fun GentleRewardNoAuthorPreview() {
    OrielleTheme {
        GentleRewardScreenContent(
            mood = "Playful",
            quote = QuoteEntity(
                id = "playful_01",
                quote = "Life is what happens to you while you're busy making other plans.",
                source = "", // No author
                mood = "Playful"
            ),
            isLoading = false,
            error = null,
            isSharing = false,
            onDone = {},
            onShare = {},
            onRetry = {}
        )
    }
}
