package com.orielle.ui.screens.sanctuary

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.orielle.ui.theme.OrielleTheme
import kotlinx.coroutines.flow.collectLatest
import com.orielle.ui.util.UiEvent
import com.orielle.ui.components.ErrorScreen

@Composable
fun SanctuaryScreen(
    viewModel: SanctuaryViewModel = hiltViewModel(),
    onNavigateToAuth: () -> Unit
) {
    val prompt by viewModel.prompt.collectAsState()
    val userReflection by viewModel.userReflection.collectAsState()
    val aiResponse by viewModel.aiResponse.collectAsState()
    val showCta by viewModel.showCta.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val errorState = remember { mutableStateOf<String?>(null) }

    // Collect error events and show in Snackbar, and set errorState for critical errors
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event: UiEvent ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                    if (event.message.contains("unknown error", ignoreCase = true)) {
                        errorState.value = event.message
                    }
                }

                UiEvent.Navigate -> TODO()
                UiEvent.NavigateUp -> TODO()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (errorState.value != null) {
            ErrorScreen(
                message = errorState.value ?: "An error occurred.",
                onRetry = {
                    errorState.value = null
                    viewModel.submitReflection()
                }
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // The UI will transition between the prompt and the response
                AnimatedContent(
                    targetState = aiResponse == null,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(1000)) togetherWith fadeOut(
                            animationSpec = tween(
                                1000
                            )
                        )
                    },
                    label = "SanctuaryContent"
                ) { isShowingPrompt ->
                    if (isShowingPrompt) {
                        PromptContent(
                            prompt = prompt,
                            userReflection = userReflection,
                            onReflectionChange = viewModel::onReflectionChange,
                            onSubmit = { viewModel.submitReflection() }
                        )
                    } else {
                        ReflectionContent(
                            aiResponse = aiResponse ?: "",
                            showCta = showCta,
                            onNavigateToAuth = onNavigateToAuth
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PromptContent(
    prompt: String,
    userReflection: String,
    onReflectionChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = userReflection,
            onValueChange = onReflectionChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            label = { Text("Your reflection...") }
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onSubmit,
            enabled = userReflection.isNotBlank()
        ) {
            Text("Share Moment")
        }
    }
}

@Composable
private fun ReflectionContent(
    aiResponse: String,
    showCta: Boolean,
    onNavigateToAuth: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = aiResponse,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = "This reflection is just for now. Your journey is saved when you create an account.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        // The CTA buttons will fade in gracefully
        AnimatedVisibility(
            visible = showCta,
            enter = fadeIn(animationSpec = tween(1000, delayMillis = 500))
        ) {
            Column {
                Button(
                    onClick = onNavigateToAuth,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Begin Your Journey")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SanctuaryScreenPreview() {
    OrielleTheme {
        SanctuaryScreen(onNavigateToAuth = {})
    }
}
