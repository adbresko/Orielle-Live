package com.orielle.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.orielle.domain.model.Response
import com.orielle.ui.components.HalfMoonShape
import com.orielle.ui.components.OrielleLogo
import com.orielle.ui.components.OriellePrimaryButton
import com.orielle.ui.components.SocialLoginOptions
import com.orielle.ui.theme.OrielleTheme
import com.orielle.ui.util.UiEvent
import kotlinx.coroutines.flow.collectLatest
import android.app.Activity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.orielle.R
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import com.orielle.ui.components.WaterDropLoading

@Composable
fun SignInScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    navigateToHome: () -> Unit,
    navigateToSignUp: () -> Unit
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val authResponse by viewModel.authResponse.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    // Google Sign-In Logic
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
                viewModel.signInWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Optionally show error
            }
        }
    }

    var showForgotDialog by remember { mutableStateOf(false) }
    var forgotEmail by remember { mutableStateOf("") }
    var forgotStatus by remember { mutableStateOf<String?>(null) }

    // Collect error events and show in Snackbar
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collectLatest { event: UiEvent ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                else -> {}
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top Logo Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 60.dp), // Slightly reduced top padding for the logo
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OrielleLogo()
            }

            // Bottom Card with Half-Moon Shape
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.70f) // CORRECTED: Standardized to match WelcomeScreen
                    .clip(HalfMoonShape())
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 24.dp), // CORRECTED: Adjusted padding
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text("Welcome Back", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(16.dp))

                    AuthFormFields(
                        displayName = "",
                        onDisplayNameChange = {},
                        email = email,
                        onEmailChange = viewModel::onEmailChange,
                        password = password,
                        onPasswordChange = viewModel::onPasswordChange,
                        isSignUp = false
                    )

                    Text(
                        text = "Forgot My Password?",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clickable { showForgotDialog = true }
                    )

                    if (showForgotDialog) {
                        AlertDialog(
                            onDismissRequest = { showForgotDialog = false },
                            title = { Text("Reset Password") },
                            text = {
                                Column {
                                    OutlinedTextField(
                                        value = forgotEmail,
                                        onValueChange = { forgotEmail = it },
                                        label = { Text("Email") }
                                    )
                                    if (forgotStatus != null) Text(forgotStatus!!, color = Color.Green)
                                }
                            },
                            confirmButton = {
                                Button(onClick = {
                                    FirebaseAuth.getInstance().sendPasswordResetEmail(forgotEmail)
                                    forgotStatus = "Reset email sent to $forgotEmail"
                                }) { Text("Send Reset Email") }
                            },
                            dismissButton = {
                                OutlinedButton(onClick = { showForgotDialog = false }) { Text("Cancel") }
                            }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    OriellePrimaryButton(
                        onClick = { viewModel.signIn() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Sign In")
                    }

                    Spacer(Modifier.height(24.dp))

                    SocialLoginOptions(
                        onGoogleSignInClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
                        onAppleSignInClick = { /* TODO */ }
                    )

                    Spacer(Modifier.weight(1f))

                    TextButton(onClick = navigateToSignUp) {
                        Text("Don't have an account? Sign Up")
                    }
                }
            }

            // Auth Response handling...
            when (val response = authResponse) {
                is Response.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        WaterDropLoading()
                    }
                }
                is Response.Success -> {
                    LaunchedEffect(Unit) { navigateToHome() }
                }
                is Response.Failure -> {}
                null -> {}
            }
        }
    }
}

// Preview remains unchanged...
@Preview(showBackground = true)
@Composable
private fun SignInScreenPreview() {
    OrielleTheme {
        SignInScreen(navigateToHome = {}, navigateToSignUp = {})
    }
}