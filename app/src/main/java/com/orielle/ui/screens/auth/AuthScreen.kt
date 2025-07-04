package com.orielle.ui.screens.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.orielle.R
import com.orielle.domain.model.Response
import com.orielle.ui.components.OrielleLogo
import com.orielle.ui.theme.OrielleTheme

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    navigateToHome: () -> Unit // Callback to navigate to the home screen
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val authResponse by viewModel.authResponse.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Sign Up", "Sign In")

    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))

            OrielleLogo()

            Spacer(Modifier.height(32.dp))

            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            AuthFormFields(
                email = email,
                onEmailChange = viewModel::onEmailChange,
                password = password,
                onPasswordChange = viewModel::onPasswordChange
            )

            if (selectedTabIndex == 1) {
                TextButton(
                    onClick = { /* TODO: Navigate to Forgot Password screen */ },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Forgot Password?")
                }
            } else {
                Spacer(modifier = Modifier.height(48.dp))
            }

            Spacer(Modifier.height(16.dp))

            val buttonText = if (selectedTabIndex == 0) "Create Account" else "Sign In"
            Button(
                onClick = {
                    if (selectedTabIndex == 0) {
                        viewModel.signUp()
                    } else {
                        viewModel.signIn()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonText)
            }

            Spacer(Modifier.height(32.dp))

            SocialLogins()

            Spacer(Modifier.weight(1f))
        }

        // Handle the response from the ViewModel
        when (val response = authResponse) {
            is Response.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is Response.Success -> {
                // On success, navigate to the home screen
                LaunchedEffect(Unit) {
                    navigateToHome()
                }
            }
            is Response.Failure -> {
                // On failure, show an error message
                val context = LocalContext.current
                LaunchedEffect(response) {
                    Toast.makeText(context, response.exception.message, Toast.LENGTH_LONG).show()
                }
            }
            null -> {
                // Initial state, do nothing
            }
        }
    }
}

@Composable
private fun SocialLogins() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Or continue with",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SocialLoginButton(iconResId = R.drawable.ic_google, onClick = { /*TODO*/ })
            SocialLoginButton(iconResId = R.drawable.ic_apple, onClick = { /*TODO*/ })
        }
    }
}

@Composable
private fun SocialLoginButton(iconResId: Int, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.size(56.dp),
        shape = MaterialTheme.shapes.medium,
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(id = iconResId),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = Color.Unspecified
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthScreenPreview() {
    OrielleTheme {
        AuthScreen(navigateToHome = {})
    }
}
