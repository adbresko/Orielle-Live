package com.orielle.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orielle.ui.theme.Typography
import com.orielle.ui.util.ScreenUtils
import com.orielle.ui.components.WaterDropLoading

/**
 * Enhanced loading states with smooth animations for better user experience.
 */

@Composable
fun SkeletonLoader(
    modifier: Modifier = Modifier,
    isLoading: Boolean = true
) {
    AnimatedVisibility(
        visible = isLoading,
        enter = fadeIn(animationSpec = tween(300)),
        exit = fadeOut(animationSpec = tween(200))
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Skeleton for greeting
            SkeletonBox(
                width = 200.dp,
                height = 24.dp,
                cornerRadius = 4.dp
            )

            // Skeleton for date
            SkeletonBox(
                width = 150.dp,
                height = 16.dp,
                cornerRadius = 4.dp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Skeleton for cards
            repeat(2) {
                SkeletonCard()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SkeletonCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkeletonBox(
                width = 120.dp,
                height = 16.dp,
                cornerRadius = 4.dp
            )
            SkeletonBox(
                width = 200.dp,
                height = 14.dp,
                cornerRadius = 4.dp
            )
            SkeletonBox(
                width = 180.dp,
                height = 14.dp,
                cornerRadius = 4.dp
            )
        }
    }
}

@Composable
fun SkeletonBox(
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    cornerRadius: androidx.compose.ui.unit.Dp = 8.dp
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surface.copy(alpha = 0.1f),
        MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset(translateAnimation.value - 1f, 0f),
        end = androidx.compose.ui.geometry.Offset(translateAnimation.value, 0f)
    )

    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clip(RoundedCornerShape(cornerRadius))
            .background(brush)
    )
}

@Composable
fun AnimatedLoadingIndicator(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated water drop
        val infiniteTransition = rememberInfiniteTransition(label = "loading")
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )

        Box(
            modifier = Modifier
                .size(ScreenUtils.responsiveImageSize(60.dp))
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
        ) {
            WaterDropLoading(
                size = ScreenUtils.responsiveImageSize(60.dp).value.toInt(),
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(ScreenUtils.responsiveSpacing() * 2))

        Text(
            text = message,
            style = Typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun SmoothContentTransition(
    isVisible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(300, easing = EaseOutCubic)) +
                slideInVertically(
                    animationSpec = tween(300, easing = EaseOutCubic),
                    initialOffsetY = { it / 4 }
                ),
        exit = fadeOut(animationSpec = tween(200, easing = EaseInCubic)) +
                slideOutVertically(
                    animationSpec = tween(200, easing = EaseInCubic),
                    targetOffsetY = { it / 4 }
                )
    ) {
        content()
    }
}

@Composable
fun ProgressiveLoadingContent(
    isLoading: Boolean,
    loadingContent: @Composable () -> Unit = { SkeletonLoader() },
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Loading content
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            loadingContent()
        }

        // Main content
        AnimatedVisibility(
            visible = !isLoading,
            enter = fadeIn(animationSpec = tween(300, delayMillis = 100)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            content()
        }
    }
}

@Composable
fun QuoteCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            SkeletonBox(width = 200.dp, height = 24.dp)
            Spacer(Modifier.height(16.dp))
            SkeletonBox(width = 150.dp, height = 20.dp)
            Spacer(Modifier.height(8.dp))
            SkeletonBox(width = 100.dp, height = 16.dp)
        }
    }
}

@Composable
fun ErrorStateWithRetry(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Something went wrong",
            style = Typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.error
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = error,
            style = Typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Try Again")
        }
    }
}