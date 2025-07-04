package com.orielle.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.orielle.domain.model.Response
import com.orielle.ui.components.OrielleLogo
import com.orielle.ui.theme.OrielleTheme

@Composable
fun EmailSignUpScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    navigateToHome: () -> Unit
) {
    val displayName by viewModel.displayName.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val authResponse by viewModel.authResponse.collectAsState()

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
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
            Button(
                onClick = { viewModel.signUp() },
                modifier = Modifier.fillMaxWidth(),
                enabled = displayName.isNotBlank() && email.isNotBlank() && password.isNotBlank()
            ) {
                Text("Create Account")
            }
        }

        when (val response = authResponse) {
            is Response.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Response.Success -> {
                LaunchedEffect(Unit) { navigateToHome() }
            }
            is Response.Failure -> {
                val context = LocalContext.current
                LaunchedEffect(response) {
                    Toast.makeText(context, "Sign-up failed. Please try again.", Toast.LENGTH_LONG).show()
                }
            }
            null -> {}
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EmailSignUpScreenPreview() {
    OrielleTheme {
        EmailSignUpScreen(navigateToHome = {})
    }
}
