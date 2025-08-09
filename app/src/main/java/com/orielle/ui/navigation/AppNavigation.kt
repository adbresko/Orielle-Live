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
import com.orielle.ui.screens.mood.MoodFinalScreen
import android.content.Context
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.EntryPointAccessors
import com.orielle.data.manager.SessionManagerImpl
import com.orielle.domain.manager.SessionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import androidx.compose.ui.platform.LocalContext
import com.orielle.ui.screens.profile.ProfileSettingsScreen
import com.orielle.ui.screens.ask.AskScreen
import com.orielle.ui.screens.ask.AskTaggingScreen
import com.orielle.ui.screens.reflect.ReflectScreen
import com.orielle.ui.screens.reflect.JournalEditorScreen
import com.orielle.ui.screens.reflect.JournalLogScreen
import com.orielle.ui.screens.reflect.JournalDetailScreen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun AppNavigation(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val isUserAuthenticated by authViewModel.isUserAuthenticated.collectAsState()
    val context = LocalContext.current
    // Use Hilt EntryPoint to get SessionManagerImpl
    val sessionManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SessionManagerEntryPoint::class.java
        ).sessionManagerImpl()
    }
    val hasSeenOnboarding by sessionManager.hasSeenOnboarding.collectAsState(initial = false)

    NavHost(
        navController = navController,
        startDestination = "splash_router", // Use a static start destination
        modifier = modifier,
        enterTransition = { fadeIn(animationSpec = tween(300)) },
        exitTransition = { fadeOut(animationSpec = tween(300)) }
    ) {
        // This is the router screen. It decides where to go first.
        composable("splash_router") {
            SplashScreenRouter(
                isUserAuthenticated = isUserAuthenticated,
                hasSeenOnboarding = hasSeenOnboarding,
                navController = navController
            )
        }

        // The authentication flow is now in its own nested navigation graph
        authGraph(navController, authViewModel)

        // The main part of the app after login
        composable("home_graph") {
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
            val moodCheckInViewModel: com.orielle.ui.screens.mood.MoodCheckInViewModel = hiltViewModel()

            MoodReflectionScreen(
                moodName = moodName,
                moodIconRes = moodIconRes,
                onSave = { mood, selectedOptions, notes ->
                    // Save the mood check-in with selected data
                    moodCheckInViewModel.saveMoodCheckIn(
                        mood = mood,
                        tags = selectedOptions,
                        notes = if (notes.isBlank()) null else notes
                    )

                    // Navigate to final screen
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
        composable("profile_settings") {
            // Get user data from ViewModel or session (replace with your actual data source)
            val homeViewModel: com.orielle.ui.screens.home.HomeViewModel = hiltViewModel()
            val uiState by homeViewModel.uiState.collectAsState()
            val userId = "TODO_USER_ID" // Replace with actual user ID from session/auth
            ProfileSettingsScreen(
                navController = navController,
                userId = userId,
                userName = uiState.userName,
                userEmail = null, // Replace with actual email if available
                profileImageUrl = null, // Replace with actual image URL if available
                onLogOut = {
                    // Log out and navigate to sign-in
                    homeViewModel.logOut(navController)
                },
                homeViewModel = homeViewModel // Pass the view model for refresh
            )
        }

        // Ask feature screens
        composable("ask") {
            AskScreen(navController = navController)
        }

        composable("ask_tagging") {
            AskTaggingScreen(navController = navController)
        }

        // Reflect/Journal screens
        composable("reflect") {
            ReflectScreen(navController = navController)
        }

        composable(
            "journal_editor?promptText={promptText}&isQuickEntry={isQuickEntry}&entryId={entryId}",
            arguments = listOf(
                navArgument("promptText") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
                navArgument("isQuickEntry") {
                    type = NavType.BoolType
                    defaultValue = false
                },
                navArgument("entryId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val promptText = backStackEntry.arguments?.getString("promptText")
            val isQuickEntry = backStackEntry.arguments?.getBoolean("isQuickEntry") ?: false
            val entryId = backStackEntry.arguments?.getString("entryId")

            JournalEditorScreen(
                navController = navController,
                promptText = promptText,
                isQuickEntry = isQuickEntry,
                entryId = entryId
            )
        }

        composable("journal_log") {
            JournalLogScreen(navController = navController)
        }

        composable(
            "journal_detail/{entryId}",
            arguments = listOf(navArgument("entryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getString("entryId") ?: ""
            JournalDetailScreen(
                navController = navController,
                entryId = entryId
            )
        }

        composable(
            "journal_tagging/{tempEntryId}",
            arguments = listOf(navArgument("tempEntryId") { type = NavType.StringType })
        ) { backStackEntry ->
            val tempEntryId = backStackEntry.arguments?.getString("tempEntryId") ?: ""
            // Reuse the Ask tagging screen for journal entries
            AskTaggingScreen(navController = navController)
        }
    }
}

// SplashScreenRouter with mood check-in logic
@Composable
fun SplashScreenRouter(
    isUserAuthenticated: Boolean?,
    hasSeenOnboarding: Boolean,
    navController: NavController,
) {
    val context = LocalContext.current
    val sessionManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            SessionManagerEntryPoint::class.java
        ).sessionManagerImpl()
    }
    var needsMoodCheckIn by remember { mutableStateOf(false) }
    var checkedMood by remember { mutableStateOf(false) }

    LaunchedEffect(isUserAuthenticated, hasSeenOnboarding) {
        if (isUserAuthenticated == true) {
            // For authenticated users, always route to home and let HomeViewModel handle check-in status
            // This way, they can see their existing mood data even if they haven't done today's check-in
            needsMoodCheckIn = false
            checkedMood = true
        } else {
            checkedMood = true
        }
    }

    if (!checkedMood) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LaunchedEffect(isUserAuthenticated, hasSeenOnboarding, needsMoodCheckIn, checkedMood) {
        if (isUserAuthenticated == true) {
            if (needsMoodCheckIn) {
                navController.navigate("mood_check_in") {
                    popUpTo("splash_router") { inclusive = true }
                }
            } else {
                navController.navigate("home_graph") {
                    popUpTo("splash_router") { inclusive = true }
                }
            }
        } else if (!hasSeenOnboarding) {
            navController.navigate("onboarding") {
                popUpTo("splash_router") { inclusive = true }
            }
        } else {
            navController.navigate("welcome") {
                popUpTo("splash_router") { inclusive = true }
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
        composable("sanctuary") {
            HomeScreen(navController = navController)
        }
    }
}

// Define Hilt EntryPoint for SessionManagerImpl
@dagger.hilt.EntryPoint
@dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
interface SessionManagerEntryPoint {
    fun sessionManagerImpl(): SessionManagerImpl
}