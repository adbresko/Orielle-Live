package com.orielle.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.orielle.ui.util.ScreenUtils

// Defines the typography for the Orielle Theme, using the custom Lora and NotoSans fonts.
val Typography = Typography(
    // For large, emotionally resonant titles (e.g., "Welcome to Orielle")
    displayLarge = TextStyle(
        fontFamily = Lora,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    // For standard page titles
    headlineLarge = TextStyle(
        fontFamily = Lora,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    // For section titles
    titleLarge = TextStyle(
        fontFamily = NotoSans,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    // For primary body text, reflections, and prompts
    bodyLarge = TextStyle(
        fontFamily = NotoSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp, // 1.5x line height for readability
        letterSpacing = 0.5.sp
    ),
    // For interactive elements like buttons
    labelLarge = TextStyle(
        fontFamily = NotoSans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    // For secondary text, captions, etc.
    bodyMedium = TextStyle(
        fontFamily = NotoSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

// Responsive typography that scales based on screen size
@Composable
fun getResponsiveTypography(): Typography {
    val scaleFactor = ScreenUtils.getTextScaleFactor()

    return Typography(
        displayLarge = Typography.displayLarge.copy(
            fontSize = (32 * scaleFactor).sp,
            lineHeight = (40 * scaleFactor).sp
        ),
        headlineLarge = Typography.headlineLarge.copy(
            fontSize = (28 * scaleFactor).sp,
            lineHeight = (36 * scaleFactor).sp
        ),
        titleLarge = Typography.titleLarge.copy(
            fontSize = (22 * scaleFactor).sp,
            lineHeight = (28 * scaleFactor).sp
        ),
        bodyLarge = Typography.bodyLarge.copy(
            fontSize = (16 * scaleFactor).sp,
            lineHeight = (24 * scaleFactor).sp,
            letterSpacing = (0.5 * scaleFactor).sp
        ),
        labelLarge = Typography.labelLarge.copy(
            fontSize = (16 * scaleFactor).sp
        ),
        bodyMedium = Typography.bodyMedium.copy(
            fontSize = (14 * scaleFactor).sp,
            lineHeight = (20 * scaleFactor).sp
        )
    )
}