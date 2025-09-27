package com.orielle.ui.screens.mood

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.FileProvider
import com.orielle.R
import com.orielle.data.local.model.QuoteEntity
import com.orielle.ui.components.OrielleScreenHeader
import com.orielle.ui.theme.*
import com.orielle.ui.util.ScreenUtils
import com.orielle.ui.util.ResponsivePreview
import java.io.File
import java.io.FileOutputStream

@Composable
fun ShareScreen(
    quote: QuoteEntity,
    mood: String,
    onBack: () -> Unit,
    viewModel: ShareViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Initialize with the quote and mood
    LaunchedEffect(quote, mood) {
        viewModel.setQuoteData(quote, mood)
    }

    ShareScreenContent(
        quote = quote,
        mood = mood,
        onBack = onBack,
        onShare = { viewModel.shareQuote(context) },
        isSharing = uiState.isSharing
    )
}

@Composable
private fun ShareScreenContent(
    quote: QuoteEntity,
    mood: String,
    onBack: () -> Unit,
    onShare: () -> Unit,
    isSharing: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(ScreenUtils.responsivePadding() * 1.5f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        OrielleScreenHeader(
            text = "Share your insight"
        )

        Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 2))

        // Preview Card
        SharePreviewCard(
            quote = quote,
            mood = mood,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(ScreenUtils.responsivePadding() * 3))

        // Action Buttons
        ActionButtons(
            onBack = onBack,
            onShare = onShare,
            isSharing = isSharing
        )
    }
}

@Composable
private fun SharePreviewCard(
    quote: QuoteEntity,
    mood: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = getMoodBackgroundColor(mood)

    Card(
        shape = RoundedCornerShape(ScreenUtils.responsivePadding() * 2),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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

            // Orielle branding
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsiveSpacing())
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_orielle_drop),
                    contentDescription = "Orielle Drop",
                    modifier = Modifier.size(ScreenUtils.responsiveIconSize(24.dp))
                )
                Text(
                    text = "Orielle",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun ActionButtons(
    onBack: () -> Unit,
    onShare: () -> Unit,
    isSharing: Boolean
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(ScreenUtils.responsivePadding()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back button
        OutlinedButton(
            onClick = onBack,
            shape = RoundedCornerShape(ScreenUtils.responsivePadding() * 2),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
        ) {
            Text(
                text = "Back",
                fontWeight = FontWeight.Medium
            )
        }

        // Share button
        Button(
            onClick = onShare,
            shape = RoundedCornerShape(ScreenUtils.responsivePadding() * 2),
            colors = ButtonDefaults.buttonColors(containerColor = StillwaterTeal),
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            enabled = !isSharing
        ) {
            if (isSharing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Share",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
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
private fun ShareScreenPreview() {
    OrielleTheme {
        ShareScreenContent(
            quote = QuoteEntity(
                id = "happy_01",
                quote = "The power of finding beauty in the humblest things makes home happy and life lovely.",
                source = "Louisa May Alcott",
                mood = "Happy"
            ),
            mood = "Happy",
            onBack = {},
            onShare = {},
            isSharing = false
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ShareScreenDarkPreview() {
    OrielleTheme {
        ShareScreenContent(
            quote = QuoteEntity(
                id = "sad_01",
                quote = "Even in our sleep, pain which cannot be forgotten falls drop by drop upon the heart.",
                source = "Aeschylus",
                mood = "Sad"
            ),
            mood = "Sad",
            onBack = {},
            onShare = {},
            isSharing = false
        )
    }
}
