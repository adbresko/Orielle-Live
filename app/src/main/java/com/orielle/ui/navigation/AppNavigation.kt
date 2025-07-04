package com.orielle.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.orielle.ui.screens.auth.AuthViewModel
import com.orielle.ui.screens.auth.EmailSignUpScreen
import com.orielle.ui.screens.auth.SignInScreen
import com.orielle.ui.screens.auth.WelcomeScreen
import com.orielle.ui.screens.onboarding.OnboardingScreen
import com.orielle.ui.screens.sanctuary.SanctuaryScreen

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel() // Get ViewModel at the top level
) {
    val navController = rememberNavController()
    val isUserAuthenticated by authViewModel.isUserAuthenticated.collectAsState()

    // Show a loading indicator while we check the auth state
    if (isUserAuthenticated == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return // Stop here until the state is determined
    }

    // Determine the start destination based on the auth state
    val startDestination = if (isUserAuthenticated == true) "home" else "onboarding"

    NavHost(
        navController = navController,
        startDestination = startDestination, // Use the dynamic start destination
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onNavigateToAuth = {
                    navController.navigate("welcome") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("welcome") {
            WelcomeScreen(
                // Pass the same AuthViewModel instance down to keep state consistent
                viewModel = authViewModel,
                onNavigateToEmailSignUp = { navController.navigate("email_signup") },
                onNavigateToSignIn = { navController.navigate("sign_in") },
                onNavigateToSanctuary = { navController.navigate("sanctuary") },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }
        composable("email_signup") {
            EmailSignUpScreen(
                viewModel = authViewModel,
                navigateToHome = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }
        composable("sign_in") {
            SignInScreen(
                viewModel = authViewModel,
                navigateToHome = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                },
                navigateToSignUp = {
                    navController.popBackStack()
                }
            )
        }
        composable("sanctuary") {
            SanctuaryScreen(
                onNavigateToAuth = {
                    navController.navigate("welcome") {
                        popUpTo("sanctuary") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen()
        }
    }
}

@Composable
fun HomeScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Welcome to Orielle!")
    }
}
