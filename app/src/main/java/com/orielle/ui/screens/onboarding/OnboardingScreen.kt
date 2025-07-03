package com.orielle.ui.screens.onboarding

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.orielle.ui.theme.OrielleTheme

@Composable
fun OnboardingScreen(onNavigateToAuth: () -> Unit) {
    // We will build the full pager UI here once assets are available.
    // For now, this is a placeholder to verify navigation is working.
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = onNavigateToAuth) {
            Text(
                text = "Let's Get Started (Placeholder)",
                style = MaterialTheme.typography.bodyLarge
            )
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
