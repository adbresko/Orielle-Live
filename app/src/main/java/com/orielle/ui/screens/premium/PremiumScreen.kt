package com.orielle.ui.screens.premium

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.orielle.ui.components.OriellePrimaryButton
import com.orielle.ui.theme.OrielleTheme
import com.orielle.ui.components.WaterDropLoading

@Composable
fun PremiumScreen(
    viewModel: PremiumViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onUpgradeComplete: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is com.orielle.ui.util.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Upgrade to Premium",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Unlock all features and enhance your journaling experience",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Premium Features
            PremiumFeature(
                title = "Unlimited Journal Entries",
                description = "Write as much as you want without any limits"
            )

            PremiumFeature(
                title = "Advanced Mood Analytics",
                description = "Get detailed insights into your emotional patterns"
            )

            PremiumFeature(
                title = "AI-Powered Reflections",
                description = "Receive personalized insights and prompts"
            )

            PremiumFeature(
                title = "Cloud Backup & Sync",
                description = "Your data is safely backed up and synced across devices"
            )

            PremiumFeature(
                title = "Priority Support",
                description = "Get help when you need it most"
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Subscription Options
            if (uiState.products.isNotEmpty()) {
                uiState.products.forEach { product ->
                    SubscriptionOption(
                        product = product,
                        isSelected = uiState.selectedProductId == product.productId,
                        onSelect = { viewModel.selectProduct(product.productId) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Upgrade Button
            OriellePrimaryButton(
                onClick = { viewModel.purchasePremium() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.selectedProductId != null && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    WaterDropLoading(
                        size = 20,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text("Upgrade Now")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Restore Purchases
            TextButton(
                onClick = { viewModel.restorePurchases() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Restore Purchases")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Terms
            Text(
                text = "By upgrading, you agree to our Terms of Service and Privacy Policy. Subscriptions auto-renew unless cancelled.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PremiumFeature(
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SubscriptionOption(
    product: com.orielle.domain.manager.SubscriptionProduct,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            CardDefaults.outlinedCardBorder()
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onSelect
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = product.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = product.price,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}