package com.orielle.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orielle.R
import com.orielle.ui.theme.OrielleTheme
import kotlinx.coroutines.launch
import com.orielle.data.manager.SessionManagerImpl
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import dagger.hilt.android.EntryPointAccessors
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.remember

// Data class to hold the content for each onboarding page
private data class OnboardingPage(
    val imageResId: Int,
    val title: String,
    val description: String,
)

/**
 * A custom shape that creates the curved top for the card.
 * This is now identical to the shape used on the WelcomeScreen for consistency.
 */
private class HalfMoonShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path().apply {
            moveTo(0f, size.height * 0.15f)
            quadraticBezierTo(
                x1 = size.width / 2, y1 = -size.height * 0.1f,
                x2 = size.width, y2 = size.height * 0.15f
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}

@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface SessionManagerEntryPoint {
    fun sessionManagerImpl(): SessionManagerImpl
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onNavigateToAuth: () -> Unit,
) {
    val context = LocalContext.current
    val sessionManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SessionManagerEntryPoint::class.java
        ).sessionManagerImpl()
    }

    val pages = listOf(
        OnboardingPage(
            imageResId = R.drawable.onboarding_preview_home,
            title = "Your Personalized Mental Wellness Companion",
            description = "Discover personalized mental health plans tailored just for you by our AI. Track your mood and explore a world of wellness resources."
        ),
        OnboardingPage(
            imageResId = R.drawable.onboarding_preview_explore,
            title = "Dive and Explore Your Path to Wellness",
            description = "Explore meditation exercises, breathing techniques, articles, courses, journals, and mindfulness resources to find your center."
        ),
        OnboardingPage(
            imageResId = R.drawable.onboarding_preview_insights,
            title = "Gain Insights and Track Progress Overtime",
            description = "Gain valuable insights into your well-being with mood tracking, growth assessments, and progress reports to support your journey."
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    // When onboarding is completed:
    LaunchedEffect(Unit) {
        sessionManager.setHasSeenOnboarding(true)
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // --- BACKGROUND ---
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = false
            ) { pageIndex ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 50.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .aspectRatio(345f / 700f),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.onboarding_phone_frame),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize()
                        )
                        Image(
                            painter = painterResource(id = pages[pageIndex].imageResId),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(all = 15.dp)
                                .clip(RoundedCornerShape(45.dp)),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            // --- FOREGROUND CARD ---
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.55f)
                    .clip(HalfMoonShape()) // Updated to use the consistent shape
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(80.dp))

                    OnboardingTextContent(
                        title = pages[pagerState.currentPage].title,
                        description = pages[pagerState.currentPage].description
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        Modifier.height(24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(pages.size) { iteration ->
                            val color =
                                if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.2f
                                )
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .size(8.dp)
                                    .background(color)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TextButton(
                            onClick = onNavigateToAuth,
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                        ) {
                            Text(
                                text = "Skip",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                        Button(
                            onClick = {
                                if (pagerState.currentPage < pages.size - 1) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                    }
                                } else {
                                    onNavigateToAuth()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(text = "Continue")
                        }
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun OnboardingTextContent(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineLarge.copy(
                textAlign = TextAlign.Center,
                lineHeight = 44.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp,
            ),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun OnboardingScreenLightPreview() {
    OrielleTheme(darkTheme = false) {
        OnboardingScreen(onNavigateToAuth = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenDarkPreview() {
    OrielleTheme(darkTheme = true) {
        OnboardingScreen(onNavigateToAuth = {})
    }
}