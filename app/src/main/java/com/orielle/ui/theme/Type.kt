package com.orielle.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.orielle.R

// Define the font families by referencing the files in res/font
val Lora = FontFamily(
    Font(R.font.lora_regular, FontWeight.Normal),
    Font(R.font.lora_regular, FontWeight.Medium),
    Font(R.font.lora_bold, FontWeight.SemiBold),
    Font(R.font.lora_bold, FontWeight.Bold)
)

val NotoSans = FontFamily(
    Font(R.font.noto_sans_regular, FontWeight.Normal),
    Font(R.font.noto_sans_medium, FontWeight.Medium)
)

// This object directly mirrors the Text Styles you created in Figma
val OrielleTypography = Typography(
    // Maps to your "Orielle/Display" style
    displayLarge = TextStyle(
        fontFamily = Lora,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp
    ),
    // Maps to your "Orielle/Headline" style
    headlineLarge = TextStyle(
        fontFamily = Lora,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    // Maps to your "Orielle/Body" style
    bodyLarge = TextStyle(
        fontFamily = NotoSans,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    // Maps to your "Orielle/Button" style
    labelLarge = TextStyle(
        fontFamily = NotoSans,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    // Maps to your "Orielle/Caption" style
    bodySmall = TextStyle(
        fontFamily = NotoSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
)