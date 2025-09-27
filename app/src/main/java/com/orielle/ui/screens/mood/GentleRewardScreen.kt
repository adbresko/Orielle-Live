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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
    onShare: (QuoteEntity, String) -> Unit,
    viewModel: GentleRewardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Initialize with the selected mood
    LaunchedEffect(mood) {
        viewModel.loadQuoteForMood(mood)
    }

    GentleRewardScreenContent(
        mood = mood,
        quote = uiState.quote,
        isLoading = uiState.isLoading,
        error = uiState.error,
        onDone = onDone,
        onShare = onShare,
        onRetry = { viewModel.loadQuoteForMood(mood) }
    )
}

@Composable
private fun GentleRewardScreenContent(
    mood: String,
    quote: QuoteEntity?,
    isLoading: Boolean,
    error: String?,
    onDone: () -> Unit,
    onShare: (QuoteEntity, String) -> Unit,
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
            text = "Your daily insight"
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
                    mood = mood,
                    onShare = { onShare(quote, mood) }
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
            onShare = if (quote != null) {
                { onShare(quote, mood) }
            } else null
        )
    }
}

@Composable
private fun QuoteCard(
    quote: QuoteEntity,
    mood: String,
    onShare: () -> Unit
) {
    val backgroundColor = getMoodBackgroundColor(mood)

    Card(
        shape = RoundedCornerShape(ScreenUtils.responsivePadding() * 2),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = ScreenUtils.responsivePadding())
            .clickable { onShare() },
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScreenUtils.responsivePadding() * 2),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mood indicator
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing())
            ) {
                Image(
                    painter = painterResource(id = getMoodIcon(mood)),
                    contentDescription = mood,
                    modifier = Modifier.size(ScreenUtils.responsiveIconSize(24.dp))
                )
                Text(
                    text = mood,
                    style = MaterialTheme.typography.labelLarge,
                    color = backgroundColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 1.5f))

            // Quote text
            Text(
                text = "\"${quote.quote}\"",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                lineHeight = 28.sp
            )

            Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 1.5f))

            // Source
            Text(
                text = "— ${quote.source}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding()))

            // Orielle drop icon
            Image(
                painter = painterResource(id = R.drawable.ic_orielle_drop),
                contentDescription = "Orielle Drop",
                modifier = Modifier.size(ScreenUtils.responsiveIconSize(32.dp))
            )
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
    onShare: (() -> Unit)?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing() * 2)
    ) {
        // Done button
        Button(
            onClick = onDone,
            shape = RoundedCornerShape(ScreenUtils.responsivePadding() * 2),
            colors = ButtonDefaults.buttonColors(containerColor = StillwaterTeal),
            modifier = Modifier
                .width(180.dp)
                .height(48.dp)
        ) {
            Text(
                text = "Done",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        // Share link
        if (onShare != null) {
            Text(
                text = "Share your insight  ➔",
                style = MaterialTheme.typography.bodyMedium,
                color = StillwaterTeal,
                modifier = Modifier
                    .clickable { onShare() }
                    .padding(ScreenUtils.responsiveSpacing())
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

@Preview(showBackground = true)
@Composable
private fun GentleRewardScreenPreview() {
    OrielleTheme {
        GentleRewardScreenContent(
            mood = "Happy",
            quote = QuoteEntity(
                id = "happy_01",
                quote = "The power of finding beauty in the humblest things makes home happy and life lovely.",
                source = "Louisa May Alcott",
                mood = "Happy"
            ),
            isLoading = false,
            error = null,
            onDone = {},
            onShare = { _, _ -> },
            onRetry = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
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
            onDone = {},
            onShare = { _, _ -> },
            onRetry = {}
        )
    }
}
