package com.orielle.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.orielle.domain.model.Response
import com.orielle.ui.components.HalfMoonShape
import com.orielle.ui.components.OrielleLogo
import com.orielle.ui.components.OriellePrimaryButton
import com.orielle.ui.components.SocialLoginOptions
import com.orielle.ui.theme.OrielleTheme

@Composable
fun EmailSignUpScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    navigateToHome: () -> Unit
) {
    val displayName by viewModel.displayName.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val hasAgreedToTerms by viewModel.hasAgreedToTerms.collectAsState()
    val authResponse by viewModel.authResponse.collectAsState()
    val context = LocalContext.current

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Background with the Half-Moon Shape
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.95f) // Adjust height as needed
                    .clip(HalfMoonShape())
                    .background(MaterialTheme.colorScheme.surface)
            )

            // Foreground Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.weight(0.5f))
                OrielleLogo()
                Spacer(Modifier.height(32.dp))
                Text("Create your account", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(24.dp))

                AuthFormFields(
                    displayName = displayName,
                    onDisplayNameChange = viewModel::onDisplayNameChange,
                    email = email,
                    onEmailChange = viewModel::onEmailChange,
                    password = password,
                    onPasswordChange = viewModel::onPasswordChange,
                    isSignUp = true
                )
                Spacer(Modifier.height(16.dp))

                TermsAndConditionsCheckbox(
                    checked = hasAgreedToTerms,
                    onCheckedChange = viewModel::onTermsAgreementChange
                )
                Spacer(Modifier.height(16.dp))

                OriellePrimaryButton(
                    onClick = { viewModel.signUp() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = displayName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && hasAgreedToTerms
                ) {
                    Text("Create Account")
                }
                Spacer(Modifier.height(24.dp))

                SocialLoginOptions(
                    onGoogleSignInClick = { /* TODO */ },
                    onAppleSignInClick = { /* TODO */ }
                )
                Spacer(Modifier.weight(1f))
            }

            when (val response = authResponse) {
                is Response.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is Response.Success -> {
                    if (response.data) {
                        LaunchedEffect(Unit) { navigateToHome() }
                    }
                }

                is Response.Failure -> {
                    val errorMessage = response.exception?.message ?: "An unknown error occurred."
                    LaunchedEffect(errorMessage) {
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                null -> {}
            }
        }
    }
}

@Composable
private fun TermsAndConditionsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        val annotatedString = buildAnnotatedString {
            append("I agree to the ")
            pushStringAnnotation(tag = "TOC", annotation = "toc_url") // Tag for clickable text
            withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append("Terms & Conditions")
            }
            pop()
        }
        ClickableText(
            text = annotatedString,
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "TOC", start = offset, end = offset)
                    .firstOrNull()?.let {
                        // TODO: Navigate to Terms & Conditions URL
                    }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmailSignUpScreenPreview() {
    OrielleTheme {
        EmailSignUpScreen(navigateToHome = {})
    }
}