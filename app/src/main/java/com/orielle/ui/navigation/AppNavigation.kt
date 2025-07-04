package com.orielle.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.orielle.ui.screens.auth.AuthViewModel
import com.orielle.ui.screens.auth.EmailSignUpScreen
import com.orielle.ui.screens.auth.SignInScreen
import com.orielle.ui.screens.auth.WelcomeScreen
import com.orielle.ui.screens.home.HomeScreen
import com.orielle.ui.screens.onboarding.OnboardingScreen
import com.orielle.ui.screens.sanctuary.SanctuaryScreen

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val isUserAuthenticated by authViewModel.isUserAuthenticated.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "splash_router", // Use a static start destination
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        // This is the router screen. It decides where to go first.
        composable("splash_router") {
            SplashScreenRouter(
                isUserAuthenticated = isUserAuthenticated,
                navController = navController
            )
        }

        // The authentication flow is now in its own nested navigation graph
        authGraph(navController, authViewModel)

        // The main part of the app after login
        composable("home_graph") {
            HomeScreen(
                onNavigateToJournalEntry = { entryId ->
                    // TODO: Navigate to journaling screen
                }
            )
        }
    }
}

@Composable
fun SplashScreenRouter(
    isUserAuthenticated: Boolean?,
    navController: NavController
) {
    // This effect will run once when the auth state is determined
    LaunchedEffect(isUserAuthenticated) {
        if (isUserAuthenticated != null) {
            val destination = if (isUserAuthenticated) "home_graph" else "auth_graph"
            navController.navigate(destination) {
                // Clear the splash router from the back stack
                popUpTo("splash_router") { inclusive = true }
            }
        }
    }

    // Show a loading indicator while we wait for the auth state
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// Defines the nested navigation graph for the entire authentication flow
fun NavGraphBuilder.authGraph(navController: NavController, authViewModel: AuthViewModel) {
    navigation(startDestination = "onboarding", route = "auth_graph") {
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
                viewModel = authViewModel,
                onNavigateToEmailSignUp = { navController.navigate("email_signup") },
                onNavigateToSignIn = { navController.navigate("sign_in") },
                onNavigateToSanctuary = { navController.navigate("sanctuary") },
                onNavigateToHome = {
                    navController.navigate("home_graph") {
                        popUpTo("auth_graph") { inclusive = true }
                    }
                }
            )
        }
        composable("email_signup") {
            EmailSignUpScreen(
                viewModel = authViewModel,
                navigateToHome = {
                    navController.navigate("home_graph") {
                        popUpTo("auth_graph") { inclusive = true }
                    }
                }
            )
        }
        composable("sign_in") {
            SignInScreen(
                viewModel = authViewModel,
                navigateToHome = {
                    navController.navigate("home_graph") {
                        popUpTo("auth_graph") { inclusive = true }
                    }
                },
                navigateToSignUp = { navController.popBackStack() }
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
    }
}