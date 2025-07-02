package com.orielle.ui.features.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.*
import com.orielle.R
import com.orielle.ui.theme.OrielleTheme
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val imageRes: Int,
    val title: String,
    val description: String
)

private val onboardingPages = listOf(
    // Page 1: "Find Your Inner Peace"
    OnboardingPage(
        imageRes = R.drawable.ic_launcher_foreground, // TODO: Replace with illustration for page 1
        title = "Find Your Inner Peace",
        description = "Discover a new way to understand your mind and find tranquility."
    ),
    // Page 2: "Dive and Explore" - THIS IS THE NEW CONTENT
    OnboardingPage(
        imageRes = R.drawable.ic_launcher_foreground, // TODO: Replace with illustration for page 2
        title = "Dive and Explore Your Path to Wellness",
        description = "Explore meditation exercises, breathing techniques, articles, courses, journals, and mindfulness resources to find your center."
    ),
    // Page 3: "Begin Your Journey"
    OnboardingPage(
        imageRes = R.drawable.ic_launcher_foreground, // TODO: Replace with illustration for page 3
        title = "Begin Your Journey",
        description = "Create an account to save your progress and unlock your personalized wellness path."
    )
)

// FIX #1: The HalfMoonShape object is now defined here.
private val HalfMoonShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height * 0.85f)
    quadraticBezierTo(
        x1 = size.width / 2f,
        y1 = size.height,
        x2 = 0f,
        y2 = size.height * 0.85f
    )
    close()
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(
    onOnboardingComplete: () -> Unit,
) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                count = onboardingPages.size,
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { pageIndex ->
                OnboardingPageUI(page = onboardingPages[pageIndex])
            }

            OnboardingControls(
                pagerState = pagerState,
                onNextClicked = {
                    if (pagerState.currentPage < pagerState.pageCount - 1) { // FIX #2: Changed .count to .pageCount
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    } else {
                        onOnboardingComplete()
                    }
                },
                onSkipClicked = onOnboardingComplete // Skip also completes onboarding
            )
        }
    }
}

@Composable
private fun OnboardingPageUI(page: OnboardingPage) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.6f)
                .clip(HalfMoonShape)
                .background(MaterialTheme.colorScheme.primary)
        )
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(0.15f))
            Image(
                painter = painterResource(id = page.imageRes),
                contentDescription = page.title,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .aspectRatio(1f)
            )
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                text = page.title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = page.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun OnboardingControls(
    pagerState: PagerState,
    onNextClicked: () -> Unit,
    onSkipClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onSkipClicked) {
            Text(
                "Skip",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
        }
        HorizontalPagerIndicator(
            pagerState = pagerState,
            activeColor = MaterialTheme.colorScheme.primary,
            inactiveColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Button(onClick = onNextClicked) {
            // FIX #2: Changed .count to .pageCount
            Text(if (pagerState.currentPage < pagerState.pageCount - 1) "Next" else "Get Started")
        }
    }
}

@Preview
@Composable
fun OnboardingScreenPreview() {
    OrielleTheme {
        OnboardingScreen(onOnboardingComplete = {})
    }
}