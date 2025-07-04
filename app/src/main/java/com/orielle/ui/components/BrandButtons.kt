package com.orielle.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.ripple.LocalRippleTheme // Still okay for custom ripple
import androidx.compose.material3.Button // M3 Button
import androidx.compose.material3.ButtonDefaults // M3 ButtonDefaults
import androidx.compose.material3.OutlinedButton // M3 OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.orielle.ui.theme.WaterRippleTheme

/**
 * A custom, branded Button that uses Orielle's theme and ripple effect.
 */
@Composable
fun OriellePrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding, // Use the general M3 Button content padding
    content: @Composable RowScope.() -> Unit
) {
    CompositionLocalProvider(LocalRippleTheme provides WaterRippleTheme) {
        Button(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            contentPadding = contentPadding,
            content = content
        )
    }
}

/**
 * A custom, branded OutlinedButton for social logins and secondary actions.
 * It uses Orielle's theme and ripple effect.
 */
@Composable
fun OrielleOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    // Let OutlinedButton use its own default if not specified,
    // or use the general ButtonDefaults.ContentPadding for consistency
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    CompositionLocalProvider(LocalRippleTheme provides WaterRippleTheme) {
        OutlinedButton(
            onClick = onClick,
            modifier = modifier,
            enabled = enabled,
            contentPadding = contentPadding, // This will use OutlinedButton's internal default if contentPadding is not overridden when calling OrielleOutlinedButton
            content = content
        )
    }
}
