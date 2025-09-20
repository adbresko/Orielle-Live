package com.orielle.ui.screens.auth

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.orielle.R
import com.orielle.domain.model.Response
import com.orielle.ui.components.HalfMoonShape
import com.orielle.ui.components.OrielleLogo
import com.orielle.ui.components.OrielleOutlinedButton
import com.orielle.ui.components.OriellePrimaryButton
import com.orielle.ui.theme.OrielleTheme
import com.orielle.ui.theme.WaterRippleTheme
import com.orielle.ui.components.WaterDropLoading
import com.orielle.ui.util.ScreenUtils

@Composable
fun WelcomeScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToEmailSignUp: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onNavigateToSanctuary: () -> Unit,
    onNavigateToHome: () -> Unit,
) {
    val context = LocalContext.current
    val authResponse by viewModel.authResponse.collectAsState()

    // Google Sign-In Logic remains the same...
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Top Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = ScreenUtils.responsivePadding() * 3.75f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OrielleLogo()
            }

            // Bottom Card with Half-Moon Shape
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.77f) // The correct height for the card
                    .clip(HalfMoonShape()) // Using the standardized shape
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // Apply our custom ripple theme to all buttons within this container
                CompositionLocalProvider(LocalRippleTheme provides WaterRippleTheme) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = ScreenUtils.responsivePadding() * 6.25f,
                                start = ScreenUtils.responsivePadding() * 1.5f,
                                end = ScreenUtils.responsivePadding() * 1.5f,
                                bottom = ScreenUtils.responsivePadding() * 2
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top // Key change: Content starts from the top
                    ) {
                        Text(
                            "How do you want to begin?",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(ScreenUtils.responsivePadding() * 2))

                        OriellePrimaryButton(
                            onClick = onNavigateToEmailSignUp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Create a Free Account")
                        }
                        Spacer(Modifier.height(ScreenUtils.responsivePadding()))

                        OrielleOutlinedButton(
                            onClick = onNavigateToSignIn,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "Sign In",
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        TextButton(onClick = onNavigateToSanctuary) {
                            Text(
                                text = "Just Explore for Now",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Handle Auth Response (no changes here)
    when (val response = authResponse) {
        is Response.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                WaterDropLoading()
            }
        }

        is Response.Success -> {
            LaunchedEffect(Unit) { onNavigateToHome() }
        }

        is Response.Failure -> {
            LaunchedEffect(response) {
                val errorMessage = response.exception?.message ?: "An unknown error occurred."
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }

        null -> {}
    }
}


@Preview(showBackground = true)
@Composable
private fun WelcomeScreenRefactoredPreview() {
    OrielleTheme {
        WelcomeScreen(
            onNavigateToEmailSignUp = {},
            onNavigateToSignIn = {},
            onNavigateToSanctuary = {},
            onNavigateToHome = {}
        )
    }
}