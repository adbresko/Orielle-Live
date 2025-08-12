package com.orielle.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orielle.ui.theme.OrielleTheme
import com.orielle.ui.util.ScreenUtils

@Composable
fun OnboardingTextContent(
    modifier: Modifier = Modifier,
    title: String,
    description: String
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = if (ScreenUtils.isSmallScreen()) 16.dp else 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TODO: Replace placeholder FontFamily.Serif with the Lora font once added to res/font
        Text(
            text = title,
            // Per Figma: 32.sp, Lora Bold
            style = MaterialTheme.typography.headlineLarge.copy(
                fontFamily = FontFamily.Serif, // Placeholder for Lora
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 44.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))
        // TODO: Replace placeholder FontFamily.SansSerif with the Urbanist font once added to res/font
        Text(
            text = description,
            // Per Figma: 18.sp, Urbanist Regular
            style = MaterialTheme.typography.bodyLarge.copy(
                fontFamily = FontFamily.SansSerif, // Placeholder for Urbanist
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                lineHeight = 28.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant // Using theme's secondary text color
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingTextContentPreview() {
    OrielleTheme {
        OnboardingTextContent(
            title = "Your Personalized Mental Wellness Companion",
            description = "Discover personalized mental health plans tailored just for you by our AI. Track your mood and explore a world of wellness resources."
        )
    }
}
