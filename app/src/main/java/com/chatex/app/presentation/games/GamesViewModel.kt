package com.chatex.app.presentation.games

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamesViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(GamesUiState())
    val uiState: StateFlow<GamesUiState> = _uiState.asStateFlow()
    
    init {
        loadGames()
    }
    
    private fun loadGames() {
        viewModelScope.launch {
            val games = listOf(
                Game.TicTacToe
                // Add more games here
            )
            _uiState.value = _uiState.value.copy(games = games, isLoading = false)
        }
    }
    
    fun onGameClick(game: Game) {
        _uiState.value = _uiState.value.copy(selectedGame = game)
    }
    
    fun clearSelectedGame() {
        _uiState.value = _uiState.value.copy(selectedGame = null)
    }
}

data class GamesUiState(
    val games: List<Game> = emptyList(),
    val isLoading: Boolean = true,
    val selectedGame: Game? = null
)
