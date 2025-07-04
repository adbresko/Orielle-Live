package com.orielle.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.orielle.ui.screens.auth.EmailSignUpScreen
import com.orielle.ui.screens.auth.SignInScreen
import com.orielle.ui.screens.auth.WelcomeScreen
import com.orielle.ui.screens.onboarding.OnboardingScreen
import com.orielle.ui.screens.sanctuary.SanctuaryScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "onboarding",
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
                navigateToHome = {
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }
        composable("sign_in") {
            SignInScreen(
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
