package com.orielle.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.orielle.ui.screens.auth.AuthScreen
import com.orielle.ui.screens.onboarding.OnboardingScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "onboarding"
    ) {
        composable("onboarding") {
            OnboardingScreen(
                onNavigateToAuth = {
                    navController.navigate("auth") {
                        // Prevent going back to onboarding from auth
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        composable("auth") {
            AuthScreen(
                navigateToHome = {
                    navController.navigate("home") {
                        // Clear the auth screen from the back stack
                        popUpTo("auth") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            // Placeholder for the main screen after login
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
