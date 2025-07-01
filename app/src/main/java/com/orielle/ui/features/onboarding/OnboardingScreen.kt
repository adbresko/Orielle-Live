package com.orielle.ui.features.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.orielle.R
import com.orielle.ui.theme.OrielleTheme

// Renaming to be more specific to this single screen from the walkthrough flow
@Composable
fun WalkthroughScreen1(
    onNextClicked: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // This is the large illustration from the top of the Figma design
            // We will use a placeholder for now.
            Image(
                painter = painterResource(id = R.drawable.orielle_drop), // Replace with your actual illustration asset
                contentDescription = "Walkthrough Illustration",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Allows the image to take up flexible space
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Headline text directly from your Figma design
            Text(
                text = "Find Your Inner Peace",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Body text directly from your Figma design
            Text(
                text = "Discover a new way to understand your mind and find tranquility.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // This would be the page indicator component
            // For now, we'll represent it with a placeholder Text
            Text(text = "● ○ ○")

            Spacer(modifier = Modifier.height(32.dp))

            // Primary "Next" button at the bottom
            Button(
                onClick = onNextClicked,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium, // Uses your rounded corner variable
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = "Next",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "id:pixel_6")
@Composable
fun WalkthroughScreen1Preview() {
    OrielleTheme {
        WalkthroughScreen1(onNextClicked = {})
    }
}