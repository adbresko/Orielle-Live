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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.orielle.domain.model.Response
import com.orielle.ui.components.OrielleLogo
import com.orielle.ui.theme.OrielleTheme

@Composable
fun SignInScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    navigateToHome: () -> Unit,
    navigateToSignUp: () -> Unit // Add navigation callback to go back
) {
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

            Text("Welcome Back", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(24.dp))

            AuthFormFields(
                displayName = "", // Not used for sign-in
                onDisplayNameChange = {}, // Not used for sign-in
                email = email,
                onEmailChange = viewModel::onEmailChange,
                password = password,
                onPasswordChange = viewModel::onPasswordChange,
                isSignUp = false // Explicitly state this is not the sign-up form
            )

            TextButton(
                onClick = { /* TODO: Navigate to Forgot Password screen */ },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot Password?")
            }

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.signIn() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign In")
            }

            Spacer(Modifier.height(8.dp))

            // Add a link to go back to the sign-up flow
            TextButton(onClick = navigateToSignUp) {
                Text("Don't have an account? Sign Up")
            }
        }

        // Handle the response from the ViewModel with improved error messages
        when (val response = authResponse) {
            is Response.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is Response.Success -> {
                LaunchedEffect(Unit) {
                    navigateToHome()
                }
            }

            is Response.Failure -> {
                val context = LocalContext.current
                // Provide human-readable error messages based on the exception type
                val message = when (response.exception) {
                    is FirebaseAuthInvalidUserException -> "No account found with this email."
                    is FirebaseAuthInvalidCredentialsException -> "Incorrect password. Please try again."
                    else -> "An unexpected error occurred."
                }
                LaunchedEffect(response) {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }

            null -> {
                // Initial state
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignInScreenPreview() {
    OrielleTheme {
        SignInScreen(navigateToHome = {}, navigateToSignUp = {})
    }
}
