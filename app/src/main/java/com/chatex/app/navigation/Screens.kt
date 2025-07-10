package com.chatex.app.navigation

sealed class Screens(val route: String) {
    // Auth
    object Login : Screens("login")
    object Register : Screens("register")
    object ForgotPassword : Screens("forgot_password")
    
    // Main
    object Home : Screens("home")
    object Chats : Screens("chats")
    object Profile : Screens("profile")
    
    // Chat
    object Chat : Screens("chat/{chatId}") {
        fun createRoute(chatId: String) = "chat/$chatId"
    }
    
    // AI Chat
    object AiChat : Screens("ai_chat")
    
    // Games
    object Games : Screens("games")
    object TicTacToe : Screens("games/tic_tac_toe")
    
    // Settings
    object Settings : Screens("settings")
    object ConnectivitySettings : Screens("settings/connectivity")
    
    companion object {
        // Bottom navigation items
        val bottomNavItems = listOf(
            Chats,
            Home,
            Profile
        )
    }
}
