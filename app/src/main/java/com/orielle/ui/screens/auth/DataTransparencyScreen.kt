package com.orielle.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.orielle.ui.components.OriellePrimaryButton

@Composable
fun DataTransparencyScreen(
    navigateToHome: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Your Privacy Matters 🛡️",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            PrivacyPoint(text = "End-to-end encryption")
            PrivacyPoint(text = "No data sold or shared")
            PrivacyPoint(text = "Delete everything anytime")
            PrivacyPoint(text = "Export your journal")

            Spacer(modifier = Modifier.weight(1f))

            OriellePrimaryButton(
                onClick = navigateToHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Start First Entry")
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = { uriHandler.openUri("https://orielle.app/privacy") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View Privacy Policy")
            }
        }
    }
}

@Composable
private fun PrivacyPoint(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}