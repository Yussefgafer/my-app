package com.chatex.app.presentation.games.tictactoe

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun TicTacToeScreen(
    onBackClick: () -> Unit,
    viewModel: TicTacToeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Tic Tac Toe", 
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleGameMode() }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Toggle Game Mode"
                        )
                    }
                    IconButton(onClick = { viewModel.onNewGame() }) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "New Game"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.Center,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Game status
            GameStatus(
                currentPlayer = uiState.currentPlayer,
                gameOver = uiState.gameOver,
                winner = uiState.winner,
                isTie = uiState.isTie,
                isSinglePlayer = uiState.isSinglePlayer,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Game board
            Board(
                board = uiState.board,
                onCellClick = { row, col ->
                    if (!uiState.gameOver) {
                        viewModel.onCellClick(row, col)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            
            // Game controls
            Button(
                onClick = { viewModel.onNewGame() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Text("New Game")
            }
            
            // Game mode toggle
            TextButton(
                onClick = { viewModel.toggleGameMode() },
                modifier = Modifier.fillMaxWidth()
            ) {
                val modeText = if (uiState.isSinglePlayer) "Single Player" else "Two Players"
                Text("Switch to ${if (uiState.isSinglePlayer) "Two Players" else "Single Player"} Mode")
            }
        }
    }
}

@Composable
private fun GameStatus(
    currentPlayer: Player,
    gameOver: Boolean,
    winner: Player,
    isTie: Boolean,
    isSinglePlayer: Boolean,
    modifier: Modifier = Modifier
) {
    val text = when {
        isTie -> "Game Over - It's a Tie!"
        gameOver -> "${winner} Wins!"
        isSinglePlayer && currentPlayer == Player.O -> "AI's Turn (${currentPlayer})"
        isSinglePlayer -> "Your Turn (${currentPlayer})"
        else -> "Player ${currentPlayer}'s Turn"
    }
    
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier.padding(8.dp)
    )
}

@Composable
private fun Board(
    board: Array<Array<Cell>>,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val cellSize = 100.dp
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        board.forEachIndexed { row, cells ->
            Row {
                cells.forEachIndexed { col, cell ->
                    val scale by animateFloatAsState(
                        targetValue = if (cell.player != Player.None) 1f else 0.9f,
                        label = "cellScale"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(cellSize)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (cell.isWinning) MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onCellClick(row, col) }
                            .scale(scale),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = cell.player.toString(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = if (cell.player == Player.X) MaterialTheme.colorScheme.primary
                                  else MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}
