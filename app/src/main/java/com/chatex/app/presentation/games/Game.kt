package com.chatex.app.presentation.games

sealed class Game(val id: String, val name: String, val description: String, val iconRes: String) {
    object TicTacToe : Game("tic_tac_toe", "Tic Tac Toe", "Classic X and O game", "❌⭕")
    // Add more games here in the future
}
