package com.chatex.app.presentation.games.tictactoe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TicTacToeViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(TicTacToeUiState())
    val uiState: StateFlow<TicTacToeUiState> = _uiState.asStateFlow()

    private val game = TicTacToeGame()

    init {
        updateGameState()
    }

    fun onCellClick(row: Int, col: Int) {
        if (game.makeMove(row, col)) {
            updateGameState()
        }
    }

    fun onNewGame() {
        game.reset()
        updateGameState()
    }

    fun toggleGameMode() {
        val newMode = !_uiState.value.isSinglePlayer
        game.reset()
        _uiState.update { it.copy(isSinglePlayer = newMode) }
        updateGameState()
    }

    private fun updateGameState() {
        val board = game.getBoard()
        val currentPlayer = game.getCurrentPlayer()
        val gameOver = game.isGameOver()
        val winner = if (gameOver) game.getWinner() else Player.None
        val isTie = game.isTie()

        _uiState.update {
            it.copy(
                board = board,
                currentPlayer = currentPlayer,
                gameOver = gameOver,
                winner = winner,
                isTie = isTie
            )
        }
    }
}

data class TicTacToeUiState(
    val board: Array<Array<Cell>> = Array(3) { row ->
        Array(3) { col -> Cell(row, col) }
    },
    val currentPlayer: Player = Player.X,
    val gameOver: Boolean = false,
    val winner: Player = Player.None,
    val isTie: Boolean = false,
    val isSinglePlayer: Boolean = true
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TicTacToeUiState

        if (!board.contentDeepEquals(other.board)) return false
        if (currentPlayer != other.currentPlayer) return false
        if (gameOver != other.gameOver) return false
        if (winner != other.winner) return false
        if (isTie != other.isTie) return false
        if (isSinglePlayer != other.isSinglePlayer) return false

        return true
    }

    override fun hashCode(): Int {
        var result = board.contentDeepHashCode()
        result = 31 * result + currentPlayer.hashCode()
        result = 31 * result + gameOver.hashCode()
        result = 31 * result + winner.hashCode()
        result = 31 * result + isTie.hashCode()
        result = 31 * result + isSinglePlayer.hashCode()
        return result
    }
}
