package com.orielle.ui.screens.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.orielle.R
import com.orielle.domain.model.Response
import com.orielle.ui.components.OrielleLogo
import com.orielle.ui.components.OrielleOutlinedButton
import com.orielle.ui.components.OriellePrimaryButton
import com.orielle.ui.theme.OrielleTheme

@Composable
fun WelcomeScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToEmailSignUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onNavigateToSanctuary: () -> Unit,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val authResponse by viewModel.authResponse.collectAsState()

    // --- Google Sign-In Logic ---
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
    }
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                val idToken = account.idToken!!
                viewModel.signInWithGoogle(idToken)
            } catch (e: ApiException) {
                Toast.makeText(
                    context,
                    "Google Sign-In failed: ${e.statusCode}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    // --- End Google Sign-In Logic ---


    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))
            OrielleLogo()
            Spacer(Modifier.height(16.dp))
            Text("Let's Get Started!", style = MaterialTheme.typography.headlineLarge)
            Text(
                "Let's dive in to your account",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(32.dp))

            SocialLoginButton(
                text = "Continue with Google",
                iconResId = R.drawable.ic_google,
                onClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) }
            )
            Spacer(Modifier.height(16.dp))
            SocialLoginButton(
                text = "Continue with Apple",
                iconResId = R.drawable.ic_apple,
                onClick = { /* TODO: Implement Apple Sign-In */ }
            )
            Spacer(Modifier.height(24.dp))
            Divider()
            Spacer(Modifier.height(24.dp))

            // Use our new custom branded button
            OriellePrimaryButton(
                onClick = onNavigateToEmailSignUp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Continue with Email")
            }
            TextButton(onClick = onNavigateToSignIn) {
                Text("Already have an account? Sign In")
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onNavigateToSanctuary) {
                Text("Just Explore")
            }
            Spacer(Modifier.height(16.dp))
        }

        // Handle the auth response from the ViewModel
        when (val response = authResponse) {
            is Response.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is Response.Success -> {
                LaunchedEffect(Unit) { onNavigateToHome() }
            }

            is Response.Failure -> {
                LaunchedEffect(response) {
                    Toast.makeText(context, "Authentication failed.", Toast.LENGTH_LONG).show()
                }
            }

            null -> {}
        }
    }
}

@Composable
private fun SocialLoginButton(text: String, iconResId: Int, onClick: () -> Unit) {
    // Use our new custom branded button
    OrielleOutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )
        Text(
            text = text,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreview() {
    OrielleTheme {
        WelcomeScreen(
            onNavigateToEmailSignUp = {},
            onNavigateToSignIn = {},
            onNavigateToSanctuary = {},
            onNavigateToHome = {}
        )
    }
}
