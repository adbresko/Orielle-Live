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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.orielle.R
import com.orielle.ui.screens.auth.AuthViewModel
import com.orielle.ui.screens.auth.DataTransparencyScreen
import com.orielle.ui.screens.auth.EmailSignUpScreen
import com.orielle.ui.screens.auth.SignInScreen
import com.orielle.ui.screens.auth.WelcomeScreen
import com.orielle.ui.screens.home.HomeScreen
import com.orielle.ui.screens.mood.MoodCheckInScreen
import com.orielle.ui.screens.mood.MoodCheckInViewModel
import com.orielle.ui.screens.mood.MoodReflectionScreen
import com.orielle.ui.screens.onboarding.OnboardingScreen
import com.orielle.ui.screens.sanctuary.SanctuaryScreen
import com.orielle.ui.screens.security.SecuritySetupScreen
import com.orielle.ui.screens.mood.MoodFinalScreen

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel(),
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
            // CORRECTED: The HomeScreen call no longer takes any parameters.
            HomeScreen(navController = navController)
        }

        // Mood check-in screen
        composable("mood_check_in") {
            val authViewModel: AuthViewModel = hiltViewModel()
            val isUserAuthenticated by authViewModel.isUserAuthenticated.collectAsState()
            val navController = navController
            // Guard: If not authenticated, redirect to auth_graph
            LaunchedEffect(isUserAuthenticated) {
                if (isUserAuthenticated == false) {
                    navController.navigate("auth_graph") {
                        popUpTo("mood_check_in") { inclusive = true }
                    }
                }
            }
            if (isUserAuthenticated == true) {
                MoodCheckInScreen(
                    onMoodSelected = { moodName ->
                        // Map mood name to icon resource
                        val moodIconRes = when (moodName) {
                            "Surprised" -> R.drawable.ic_surprised
                            "Happy" -> R.drawable.ic_happy
                            "Sad" -> R.drawable.ic_sad
                            "Playful" -> R.drawable.ic_playful
                            "Angry" -> R.drawable.ic_angry
                            "Shy" -> R.drawable.ic_shy
                            "Frustrated" -> R.drawable.ic_frustrated
                            "Scared" -> R.drawable.ic_scared
                            "Peaceful" -> R.drawable.ic_peaceful
                            else -> R.drawable.ic_happy
                        }
                        navController.navigate("mood_reflection/$moodName/$moodIconRes")
                    },
                    onSkip = {
                        navController.navigate("home_graph") {
                            popUpTo("mood_check_in") { inclusive = true }
                        }
                    }
                )
            }
        }
        // Mood reflection screen
        composable(
            route = "mood_reflection/{moodName}/{moodIconRes}",
            arguments = listOf(
                navArgument("moodName") { type = NavType.StringType },
                navArgument("moodIconRes") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val moodName = backStackEntry.arguments?.getString("moodName") ?: "Happy"
            val moodIconRes = backStackEntry.arguments?.getInt("moodIconRes") ?: R.drawable.ic_happy
            MoodReflectionScreen(
                moodName = moodName,
                moodIconRes = moodIconRes,
                onSave = { _, _, _ ->
                    navController.navigate("mood_final") {
                        popUpTo("mood_reflection/$moodName/$moodIconRes") { inclusive = true }
                    }
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        // Final saved for today screen
        composable("mood_final") {
            MoodFinalScreen(
                onDone = {
                    navController.navigate("home_graph") {
                        popUpTo("mood_final") { inclusive = true }
                    }
                },
                onShare = {
                    // TODO: Implement share functionality
                }
            )
        }
    }
}

// SplashScreenRouter with mood check-in logic
@Composable
fun SplashScreenRouter(
    isUserAuthenticated: Boolean?,
    navController: NavController,
) {
    val moodCheckInViewModel: MoodCheckInViewModel = hiltViewModel()
    val uiState by moodCheckInViewModel.uiState.collectAsState()

    LaunchedEffect(isUserAuthenticated) {
        if (isUserAuthenticated != null) {
            if (isUserAuthenticated) {
                navController.navigate("mood_check_in") {
                    popUpTo("splash_router") { inclusive = true }
                }
            } else {
                navController.navigate("auth_graph") {
                    popUpTo("splash_router") { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

// authGraph remains the same
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
                onNavigateToSanctuary = {
                    navController.navigate("home_graph") {
                        popUpTo("auth_graph") { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate("home_graph") {
                        popUpTo("auth_graph") { inclusive = true }
                    }
                }
            )
        }
        // --- THIS IS THE FIX FOR ERROR #1 ---
        composable("email_signup") {
            EmailSignUpScreen(
                viewModel = authViewModel,
                navController = navController, // Pass the NavController
                navigateToSignIn = { navController.navigate("sign_in") }
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
        // --- THIS IS THE FIX FOR ERROR #2 ---
        composable("data_transparency_screen") {
            DataTransparencyScreen(
                navigateToHome = {
                    // --- THIS IS THE CHANGE ---
                    // Instead of going home, we now go to the security setup screen.
                    navController.navigate("security_setup_screen") {
                        // We pop the transparency screen off the back stack so the user can't go back to it.
                        popUpTo("data_transparency_screen") { inclusive = true }
                    }
                }
            )
        }

        // --- NEW SCREEN ADDED TO THE GRAPH ---
        composable("security_setup_screen") {
            SecuritySetupScreen(
                navigateToHome = {
                    navController.navigate("home_graph") {
                        popUpTo("auth_graph") { inclusive = true }
                    }
                }
            )
        }
        composable("sanctuary") {
            HomeScreen()
        }
    }
}