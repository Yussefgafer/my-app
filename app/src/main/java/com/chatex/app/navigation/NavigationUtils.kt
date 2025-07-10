package com.chatex.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.chatex.app.presentation.MainUiState
import com.chatex.app.presentation.auth.AuthScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest

/**
 * Sealed class representing the different navigation routes in the app
 */
sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object Chats : Screen("chats")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    
    companion object {
        // List of all screens that require authentication
        val authenticatedScreens = listOf(Chats, Profile, Settings)
    }
}

/**
 * Navigation options for navigating to a destination
 */
fun NavController.navigate(
    route: String,
    options: NavOptions? = null,
    builder: NavOptions.Builder.() -> Unit = {}
) {
    val navOptions = options ?: NavOptions.Builder().apply(builder).build()
    navigate(route, navOptions)
}

/**
 * Composable that handles authentication state and navigation
 */
@Composable
fun HandleNavigation(
    authState: MainUiState,
    navController: NavHostController,
    onNavigateToAuth: () -> Unit,
    onNavigateToMain: () -> Unit
) {
    LaunchedEffect(authState) {
        when (authState) {
            is MainUiState.Unauthenticated -> {
                // If current destination is not auth, navigate to auth
                if (navController.currentDestination?.route != Screen.Auth.route) {
                    onNavigateToAuth()
                }
            }
            is MainUiState.Authenticated -> {
                // If coming from auth, navigate to main
                if (navController.currentDestination?.route == Screen.Auth.route) {
                    onNavigateToMain()
                }
            }
            is MainUiState.Loading -> {
                // Show loading state if needed
            }
        }
    }
}

/**
 * Composable that protects a route with authentication
 */
fun NavGraphBuilder.authComposable(
    route: String,
    content: @Composable () -> Unit
) {
    composable(route) {
        val navController = rememberNavController()
        val context = LocalContext.current
        
        // In a real app, you would get this from your ViewModel
        val authState = remember { /* Get auth state from ViewModel */ }
        
        HandleNavigation(
            authState = authState,
            navController = navController,
            onNavigateToAuth = {
                navController.navigate(Screen.Auth.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },
            onNavigateToMain = {
                navController.navigate(Screen.Chats.route) {
                    popUpTo(navController.graph.startDestinationId) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
        )
        
        if (authState is MainUiState.Authenticated) {
            content()
        }
    }
}

/**
 * Extension function to observe navigation events
 */
@Composable
fun NavController.observeNavigationEvents(
    onNavigate: (String) -> Unit
) {
    val currentBackStackEntry = currentBackStackEntry
    
    LaunchedEffect(currentBackStackEntry) {
        // Observe navigation events
        currentBackStackEntry?.savedStateHandle?.getStateFlow<Boolean>("navigate", false)
            ?.collectLatest { shouldNavigate ->
                if (shouldNavigate) {
                    onNavigate("destination") // Replace with actual destination
                    currentBackStackEntry.savedStateHandle["navigate"] = false
                }
            }
    }
}

/**
 * Extension function to handle back press
 */
fun NavController.handleBackPress() {
    if (previousBackStackEntry != null) {
        navigateUp()
    } else {
        // Handle app exit or show exit dialog
    }
}

/**
 * Navigation arguments for screens
 */
object NavArgs {
    const val USER_ID = "userId"
    const val CHAT_ID = "chatId"
    
    // Add more navigation arguments as needed
}

/**
 * Navigation routes with arguments
 */
object NavRoutes {
    const val CHAT_DETAILS = "chat/{${NavArgs.CHAT_ID}}"
    const val USER_PROFILE = "user/{${NavArgs.USER_ID}}"
    
    // Build route with arguments
    fun buildRoute(baseRoute: String, vararg args: Pair<String, Any>): String {
        return args.fold(baseRoute) { route, (key, value) ->
            route.replace("{$key}", value.toString())
        }
    }
}
