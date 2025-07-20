package com.orielle.ui.screens.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.orielle.R
import com.orielle.domain.model.Response
import com.orielle.ui.components.HalfMoonShape
import com.orielle.ui.components.OrielleLogo
import com.orielle.ui.components.OriellePrimaryButton
import com.orielle.ui.components.SocialLoginOptions
import com.orielle.ui.theme.OrielleTheme
import com.orielle.ui.util.UiEvent
import kotlinx.coroutines.flow.collectLatest

@Composable
fun EmailSignUpScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    navigateToSignIn: () -> Unit,
    navController: NavController,
) {
    val email by viewModel.email.collectAsState()
    val firstName by viewModel.firstName.collectAsState()
    val password by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val hasAgreedToTerms by viewModel.hasAgreedToTerms.collectAsState()
    val authResponse by viewModel.authResponse.collectAsState()
    val passwordStrength by viewModel.passwordStrength.collectAsState()
    val emailError by viewModel.emailError.collectAsState()
    val passwordError by viewModel.passwordError.collectAsState()
    val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()
    val isNewUser by viewModel.isNewUserAfterSignUp.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }

    // Event Handling
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                else -> {}
            }
        }
    }

    LaunchedEffect(authResponse) {
        if (authResponse is Response.Success) {
            if (isNewUser) {
                // If it's a new user, go to the transparency card screen
                navController.navigate("data_transparency_screen") {
                    popUpTo("auth_graph") { inclusive = true }
                }
            } else {
                // Otherwise, go straight home
                navController.navigate("home_graph") {
                    popUpTo("auth_graph") { inclusive = true }
                }
            }
        }
    }
    // Google Sign-In Logic
    val gso = remember {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail().build()
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
                // TODO: Handle Google Sign-In failure
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 60.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OrielleLogo()
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.77f)
                    .clip(HalfMoonShape())
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 80.dp, start = 24.dp, end = 24.dp, bottom = 24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Text("Create your account", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(20.dp))

                    OutlinedTextField(
                        value = firstName,
                        onValueChange = viewModel::onFirstNameChange,
                        label = { Text("First Name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = viewModel::onEmailChange,
                        label = { Text("Email Address") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = emailError !is AuthFieldError.None,
                        supportingText = {
                            when (val error = emailError) {
                                is AuthFieldError.EmailAlreadyInUse -> {
                                    EmailInUseSupportingText(
                                        message = error.message,
                                        onSignInClick = navigateToSignIn
                                    )
                                }
                                is AuthFieldError.SimpleError -> {
                                    Text(error.message, color = MaterialTheme.colorScheme.error)
                                }
                                is AuthFieldError.None -> {}
                            }
                        },
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = viewModel::onPasswordChange,
                        label = { Text("Create Password") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = passwordError !is AuthFieldError.None,
                        supportingText = {
                            // --- FIX #1 IS HERE ---
                            val error = passwordError
                            if (error is AuthFieldError.SimpleError) {
                                Text(error.message, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(image, "Toggle password visibility")
                            }
                        },
                        singleLine = true
                    )
                    PasswordStrengthIndicator(strength = passwordStrength)

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = viewModel::onConfirmPasswordChange,
                        label = { Text("Confirm Password") },
                        modifier = Modifier.fillMaxWidth(),
                        isError = confirmPasswordError !is AuthFieldError.None,
                        supportingText = {
                            // --- FIX #2 IS HERE ---
                            val error = confirmPasswordError
                            if (error is AuthFieldError.SimpleError) {
                                Text(error.message, color = MaterialTheme.colorScheme.error)
                            }
                        },
                        visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            val image = if (isConfirmPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                            IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                                Icon(image, "Toggle password visibility")
                            }
                        },
                        singleLine = true
                    )

                    //Spacer(Modifier.height(8.dp))

                    TermsAndConditionsCheckbox(
                        checked = hasAgreedToTerms,
                        onCheckedChange = viewModel::onTermsAgreementChange
                    )
                    Spacer(Modifier.height(20.dp))

                    OriellePrimaryButton(
                        onClick = viewModel::signUp,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = firstName.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank() && hasAgreedToTerms
                    ) {
                        Text("Create Account")
                    }

                    Spacer(Modifier.height(20.dp))

                    SocialLoginOptions(
                        onGoogleSignInClick = { googleSignInLauncher.launch(googleSignInClient.signInIntent) },
                        onAppleSignInClick = { /* TODO */ }
                    )
                }
            }

            if (authResponse is Response.Loading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun EmailInUseSupportingText(
    message: String,
    onSignInClick: () -> Unit
) {
    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
            append("$message ")
        }
        pushStringAnnotation(tag = "SIGN_IN", annotation = "sign_in")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append("Sign in instead?")
        }
        pop()
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodySmall,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "SIGN_IN", start = offset, end = offset)
                .firstOrNull()?.let {
                    onSignInClick()
                }
        }
    )
}

@Composable
private fun PasswordStrengthIndicator(strength: PasswordStrength) {
    if (strength !is PasswordStrength.None) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape)
                    .background(if (strength is PasswordStrength.Weak || strength is PasswordStrength.Medium || strength is PasswordStrength.Strong) MaterialTheme.colorScheme.primary else Color.LightGray)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape)
                    .background(if (strength is PasswordStrength.Medium || strength is PasswordStrength.Strong) MaterialTheme.colorScheme.primary else Color.LightGray)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape)
                    .background(if (strength is PasswordStrength.Strong) MaterialTheme.colorScheme.primary else Color.LightGray)
            )
            Text(
                text = when (strength) {
                    is PasswordStrength.Weak -> "Weak"
                    is PasswordStrength.Medium -> "Medium"
                    is PasswordStrength.Strong -> "Strong"
                    else -> ""
                },
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TermsAndConditionsCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    val annotatedString = buildAnnotatedString {
        append("I agree to the ")
        pushStringAnnotation(tag = "TOC", annotation = "https://orielle.app/terms")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append("Terms & Conditions")
        }
        pop()
        append(" and ")
        pushStringAnnotation(tag = "PRIVACY", annotation = "https://orielle.app/privacy")
        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
            append("Privacy Policy")
        }
        pop()
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        ClickableText(
            text = annotatedString,
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "TOC", start = offset, end = offset)
                    .firstOrNull()?.let { uriHandler.openUri(it.item) }

                annotatedString.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                    .firstOrNull()?.let { uriHandler.openUri(it.item) }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun EmailSignUpScreenPreview() {
    OrielleTheme {
        // We pass a dummy NavController for the preview to build.
        EmailSignUpScreen(
            navController = rememberNavController(),
            navigateToSignIn = {}
        )
    }
}