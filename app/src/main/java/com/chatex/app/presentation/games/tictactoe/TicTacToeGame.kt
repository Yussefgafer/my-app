package com.chatex.app.presentation.games.tictactoe

sealed class Player {
    object X : Player()
    object O : Player()
    object None : Player()
    
    override fun toString(): String = when (this) {
        X -> "X"
        O -> "O"
        None -> " "
    }
}

data class Cell(
    val row: Int,
    val col: Int,
    var player: Player = Player.None,
    var isWinning: Boolean = false
)

class TicTacToeGame(private val isSinglePlayer: Boolean = true) {
    private val board = Array(3) { row ->
        Array(3) { col -> Cell(row, col) }
    }
    
    private var currentPlayer: Player = Player.X
    private var gameOver = false
    private var winner: Player = Player.None
    private var isTie = false
    
    private val winningCombinations = listOf(
        // Rows
        listOf(0 to 0, 0 to 1, 0 to 2),
        listOf(1 to 0, 1 to 1, 1 to 2),
        listOf(2 to 0, 2 to 1, 2 to 2),
        // Columns
        listOf(0 to 0, 1 to 0, 2 to 0),
        listOf(0 to 1, 1 to 1, 2 to 1),
        listOf(0 to 2, 1 to 2, 2 to 2),
        // Diagonals
        listOf(0 to 0, 1 to 1, 2 to 2),
        listOf(0 to 2, 1 to 1, 2 to 0)
    )
    
    fun makeMove(row: Int, col: Int): Boolean {
        if (gameOver || board[row][col].player != Player.None) {
            return false
        }
        
        board[row][col].player = currentPlayer
        
        if (checkWin()) {
            gameOver = true
            winner = currentPlayer
            return true
        }
        
        if (isBoardFull()) {
            gameOver = true
            isTie = true
            return true
        }
        
        currentPlayer = if (currentPlayer == Player.X) Player.O else Player.X
        
        // If single player and it's AI's turn
        if (isSinglePlayer && currentPlayer == Player.O) {
            makeAIMove()
        }
        
        return true
    }
    
    private fun makeAIMove() {
        // Simple AI: choose first available cell
        for (row in 0..2) {
            for (col in 0..2) {
                if (board[row][col].player == Player.None) {
                    makeMove(row, col)
                    return
                }
            }
        }
    }
    
    private fun checkWin(): Boolean {
        for (combination in winningCombinations) {
            val cells = combination.map { (row, col) -> board[row][col] }
            if (cells.all { it.player == currentPlayer && it.player != Player.None }) {
                cells.forEach { it.isWinning = true }
                return true
            }
        }
        return false
    }
    
    private fun isBoardFull(): Boolean {
        return board.all { row -> row.all { it.player != Player.None } }
    }
    
    fun getBoard(): Array<Array<Cell>> = board
    
    fun getCurrentPlayer(): Player = currentPlayer
    
    fun isGameOver(): Boolean = gameOver
    
    fun getWinner(): Player = winner
    
    fun isTie(): Boolean = isTie
    
    fun reset() {
        for (row in 0..2) {
            for (col in 0..2) {
                board[row][col] = Cell(row, col)
            }
        }
        currentPlayer = Player.X
        gameOver = false
        winner = Player.None
        isTie = false
    }
}
