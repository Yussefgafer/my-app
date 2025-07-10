package com.chatex.app.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.chatex.app.presentation.ai.AiChatScreen
import com.chatex.app.presentation.auth.ForgotPasswordScreen
import com.chatex.app.presentation.auth.LoginScreen
import com.chatex.app.presentation.auth.RegisterScreen
import com.chatex.app.presentation.chat.ChatScreen
import com.chatex.app.presentation.games.GamesScreen
import com.chatex.app.presentation.games.tictactoe.TicTacToeScreen
import com.chatex.app.presentation.home.HomeScreen
import com.chatex.app.presentation.profile.ProfileScreen
import com.chatex.app.presentation.settings.ConnectivitySettingsScreen
import com.chatex.app.presentation.settings.SettingsScreen

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screens.Login.route
) {
    val actions = remember(navController) { NavActions(navController) }
    
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        // Auth Flow
        composable(Screens.Login.route) {
            LoginScreen(
                onLoginSuccess = actions.navigateToMain,
                onRegisterClick = { navController.navigate(Screens.Register.route) },
                onForgotPasswordClick = { navController.navigate(Screens.ForgotPassword.route) }
            )
        }
        
        composable(Screens.Register.route) {
            RegisterScreen(
                onRegisterSuccess = actions.navigateToMain,
                onBackClick = { navController.navigateUp() }
            )
        }
        
        composable(Screens.ForgotPassword.route) {
            ForgotPasswordScreen(
                onBackClick = { navController.navigateUp() },
                onResetClick = { /* Handle password reset */ }
            )
        }
        
        // Main Flow
        composable(Screens.Home.route) {
            HomeScreen(
                onNavigateToAiChat = { navController.navigate(Screens.AiChat.route) },
                onNavigateToGames = { navController.navigate(Screens.Games.route) }
            )
        }
        
        composable(Screens.Chats.route) {
            // TODO: Implement Chats screen
        }
        
        composable(Screens.Profile.route) {
            ProfileScreen(
                onSettingsClick = { navController.navigate(Screens.Settings.route) },
                onLogout = { /* Handle logout */ }
            )
        }
        
        // Chat
        composable(
            route = Screens.Chat.route,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
            ChatScreen(
                chatId = chatId,
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // AI Chat
        composable(Screens.AiChat.route) {
            AiChatScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // Games
        composable(Screens.Games.route) {
            GamesScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
        
        composable(Screens.TicTacToe.route) {
            TicTacToeScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
        
        // Settings
        composable(Screens.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.navigateUp() },
                onConnectivitySettingsClick = { navController.navigate(Screens.ConnectivitySettings.route) }
            )
        }
        
        composable(Screens.ConnectivitySettings.route) {
            ConnectivitySettingsScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}

class NavActions(private val navController: NavHostController) {
    fun navigateToMain() {
        navController.navigate(Screens.Home.route) {
            popUpTo(Screens.Login.route) { inclusive = true }
        }
    }
    
    fun navigateToChat(chatId: String) {
        navController.navigate(Screens.Chat.createRoute(chatId))
    }
}
