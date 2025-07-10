package com.chatex.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.chatex.app.presentation.MainViewModel
import com.chatex.app.presentation.MainUiState
import com.chatex.app.presentation.auth.AuthScreen
import com.chatex.app.presentation.user.ProfileScreen
import com.chatex.app.ui.screens.AllChatsScreen
import com.chatex.app.ui.screens.ConnectivitySettingsScreen
import com.chatex.app.ui.theme.ChateXTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Chats : Screen("chats", "Chats", Icons.Default.Chat)
    object Profile : Screen("profile", "Profile", Icons.Default.AccountCircle)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChateXApp()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChateXApp() {
    ChateXTheme {
        val viewModel: MainViewModel = hiltViewModel()
        val uiState by viewModel.uiState.collectAsState()
        val navController = rememberNavController()
        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val items = listOf(Screen.Chats, Screen.Profile, Screen.Settings)
        
        // Handle authentication state changes
        LaunchedEffect(uiState) {
            when (uiState) {
                is MainUiState.Unauthenticated -> {
                    // Navigate to auth screen if not already there
                    if (navController.currentDestination?.route != "auth") {
                        navController.navigate("auth") {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
                is MainUiState.Authenticated -> {
                    // Navigate to main screen if coming from auth
                    if (navController.currentDestination?.route == "auth") {
                        navController.navigate(Screen.Chats.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
                is MainUiState.Loading -> {
                    // Show loading state
                }
            }
        }

        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                if (uiState is MainUiState.Authenticated) {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    
                    NavigationBar {
                        items.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = screen.title) },
                                label = { Text(screen.title) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        // Pop up to the start destination of the graph to
                                        // avoid building up a large stack of destinations
                                        // on the back stack as users select items
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        // Avoid multiple copies of the same destination when
                                        // reselecting the same item
                                        launchSingleTop = true
                                        // Restore state when reselecting a previously selected item
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = if (uiState is MainUiState.Authenticated) Screen.Chats.route else "auth",
                modifier = Modifier.padding(innerPadding)
            ) {
                // Auth Screen
                composable("auth") {
                    AuthScreen(
                        onSignInSuccess = {
                            viewModel.refreshUserProfile()
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
                }

                // Main Screens (only accessible when authenticated)
                composable(Screen.Chats.route) {
                    AllChatsScreen(
                        onChatClick = { chatId ->
                            // Navigate to chat
                        },
                        onSearchClick = {
                            // Handle search
                        },
                        onOptionsClick = {
                            // Show options menu
                        }
                    )
                }

                composable(Screen.Profile.route) {
                    ProfileScreen(
                        onBackClick = {
                            navController.navigateUp()
                        },
                        onEditProfilePicture = {
                            // Handle profile picture edit
                        }
                    )
                }

                composable(Screen.Settings.route) {
                    ConnectivitySettingsScreen(
                        onBackClick = {
                            navController.navigateUp()
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ChateXAppPreview() {
    ChateXTheme {
        ChateXApp()
    }
}