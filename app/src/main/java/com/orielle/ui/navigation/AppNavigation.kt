package com.orielle.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.orielle.ui.screens.auth.AuthScreenPlaceholder // We will create this shortly
import com.orielle.ui.screens.onboarding.OnboardingScreen

// Defines the routes for our application
object Routes {
    const val ONBOARDING = "onboarding"
    const val AUTH = "authentication"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.ONBOARDING) {
        composable(Routes.ONBOARDING) {
            // This is the screen composable for the onboarding flow.
            // We pass it a function (lambda) so it can tell the NavHost to navigate.
            OnboardingScreen(
                onNavigateToAuth = {
                    navController.navigate(Routes.AUTH) {
                        // This prevents the user from pressing "back" to return to the onboarding screens
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.AUTH) {
            // This is the main authentication screen (Sign In/Sign Up).
            // For now, we'll use a placeholder.
            AuthScreenPlaceholder()
        }
    }
}
