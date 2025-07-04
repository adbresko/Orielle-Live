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
import com.orielle.ui.screens.home.HomeScreen
import com.orielle.ui.screens.onboarding.OnboardingScreen
import com.orielle.ui.screens.sanctuary.SanctuaryScreen

@Composable
fun AppNavigation(
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val isUserAuthenticated by authViewModel.isUserAuthenticated.collectAsState()

    if (isUserAuthenticated == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val startDestination = if (isUserAuthenticated == true) "home" else "onboarding"

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        // ... (onboarding, welcome, email_signup, sign_in, sanctuary composables remain the same)

        composable("home") {
            HomeScreen(
                onNavigateToJournalEntry = { entryId ->
                    // Navigate to the journaling screen.
                    // If entryId is null, it's a new entry. If it has a value, we're editing an existing one.
                    navController.navigate("journaling" + if (entryId != null) "?id=$entryId" else "")
                }
            )
        }

        composable("journaling?id={id}") { backStackEntry ->
            // Placeholder for the screen where the user writes/edits a journal entry.
            // We will build this screen next.
            val entryId = backStackEntry.arguments?.getString("id")
            JournalingScreenPlaceholder(entryId = entryId)
        }
    }
}

@Composable
fun JournalingScreenPlaceholder(entryId: String?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(if (entryId == null) "Creating New Journal Entry" else "Editing Entry: $entryId")
    }
}
