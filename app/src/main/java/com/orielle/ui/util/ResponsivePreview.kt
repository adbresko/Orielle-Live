package com.orielle.ui.util

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider

@Preview(
    name = "Small Screen (Pixel 3/4 - U14/U15)",
    widthDp = 320,
    heightDp = 568,
    showBackground = true
)
@Preview(
    name = "Medium Screen (Pixel 9 Pro - U16)",
    widthDp = 412,
    heightDp = 915,
    showBackground = true
)
@Preview(
    name = "Large Screen (Tablet)",
    widthDp = 600,
    heightDp = 800,
    showBackground = true
)
annotation class ResponsivePreview

@Preview(
    name = "Pixel 3/4 (U14/U15)",
    widthDp = 320,
    heightDp = 568,
    showBackground = true
)
annotation class SmallScreenPreview

@Preview(
    name = "Pixel 9 Pro (U16)",
    widthDp = 412,
    heightDp = 915,
    showBackground = true
)
annotation class MediumScreenPreview

@Preview(
    name = "Tablet",
    widthDp = 600,
    heightDp = 800,
    showBackground = true
)
annotation class LargeScreenPreview
