package com.orielle.ui.screens.security

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.orielle.ui.components.OriellePrimaryButton
import com.orielle.ui.theme.OrielleTheme

@Composable
fun SecuritySetupScreen(
    viewModel: SecuritySetupViewModel = hiltViewModel(),
    navigateToHome: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            Text(
                text = "Secure Your Journal 🔐",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Add an extra layer of privacy to your thoughts.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            // The Biometric option is only shown if the device supports it.
            if (uiState.isBiometricAuthAvailable) {
                SecurityOptionRow(
                    title = "Enable Face ID/Touch ID",
                    subtitle = "Recommended for quick access",
                    checked = uiState.isBiometricAuthEnabled,
                    onCheckedChange = viewModel::onBiometricAuthToggled
                )
            }

            SecurityOptionRow(
                title = "Add PIN backup",
                subtitle = "Optional",
                checked = false, // Placeholder for future implementation
                onCheckedChange = {},
                enabled = false // Disabled for now
            )

            SecurityOptionRow(
                title = "Enable cloud backup",
                subtitle = "End-to-end encrypted",
                checked = true, // Placeholder for future implementation
                onCheckedChange = {},
                enabled = false // Disabled for now
            )

            Spacer(modifier = Modifier.weight(1f))

            OriellePrimaryButton(
                onClick = {
                    viewModel.onCompleteSetup()
                    navigateToHome()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = {
                    viewModel.onCompleteSetup() // Still save the default settings
                    navigateToHome()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Skip for Now")
            }
        }
    }
}

@Composable
private fun SecurityOptionRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (enabled) LocalContentColor.current else MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.38f
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = 0.38f
                )
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}

@Preview(showBackground = true)
@Composable
fun SecuritySetupScreenPreview() {
    OrielleTheme {
        SecuritySetupScreen(navigateToHome = {})
    }
}