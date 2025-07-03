package com.orielle.ui.screens.onboarding

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orielle.R
import com.orielle.ui.components.OnboardingTextContent
import com.orielle.ui.theme.OrielleTheme
import kotlinx.coroutines.launch

// Data class to hold the content for each onboarding page
private data class OnboardingPage(
    val imageResId: Int,
    val title: String,
    val description: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onNavigateToAuth: () -> Unit) {
    // Define the content for all three pages
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

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // The top part of the screen, weighted to take up most of the space
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Pager for the image content
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth()
                ) { pageIndex ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // This Box overlays the screenshot on the phone frame
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 32.dp)
                                .aspectRatio(0.5f), // Approximate aspect ratio of a phone
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.onboarding_phone_frame),
                                contentDescription = null, // Decorative
                                modifier = Modifier.fillMaxSize()
                            )
                            Image(
                                painter = painterResource(id = pages[pageIndex].imageResId),
                                contentDescription = null, // Decorative
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp), // Adjust padding to fit screenshot in frame
                                contentScale = ContentScale.FillBounds
                            )
                        }

                        Spacer(modifier = Modifier.height(48.dp))

                        // Text content that changes with the pager
                        OnboardingTextContent(
                            title = pages[pageIndex].title,
                            description = pages[pageIndex].description
                        )
                    }
                }
            }

            // The bottom part of the screen for controls
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pager Indicator
                Row(
                    Modifier.height(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pages.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .size(8.dp)
                                .background(color) // <-- CORRECTED THIS LINE
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Continue Button
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
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(text = "Continue")
                }

                // Skip Button
                TextButton(
                    onClick = onNavigateToAuth,
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(text = "Skip", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingScreenPreview() {
    OrielleTheme {
        OnboardingScreen(onNavigateToAuth = {})
    }
}
