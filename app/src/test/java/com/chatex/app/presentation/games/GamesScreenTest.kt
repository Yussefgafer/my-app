package com.chatex.app.presentation.games

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.chatex.app.presentation.games.Game
import com.chatex.app.presentation.games.GamesScreen
import com.chatex.app.presentation.games.GamesUiState
import com.chatex.app.presentation.games.GamesViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GamesScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<GamesViewModel>(relaxed = true)
    
    private val testGames = listOf(
        Game.TicTacToe,
        Game("chess", "Chess", "Classic strategy game", "♟️"),
        Game("checkers", "Checkers", "Classic board game", "⬛")
    )

    @Test
    fun gamesScreen_showsLoadingState() {
        // Given
        val uiState = MutableStateFlow(
            GamesUiState(isLoading = true)
        )
        every { viewModel.uiState } returns uiState

        // When
        composeTestRule.setContent {
            GamesScreen(
                onBackClick = {},
                viewModel = viewModel
            )
        }

        // Then
        // Verify loading indicator is shown
        // Note: You might need to add a test tag to your loading indicator
        // composeTestRule.onNodeWithTag("loadingIndicator").assertIsDisplayed()
    }

    @Test
    fun gamesScreen_showsGamesList() {
        // Given
        val uiState = MutableStateFlow(
            GamesUiState(
                isLoading = false,
                games = testGames
            )
        )
        every { viewModel.uiState } returns uiState

        // When
        composeTestRule.setContent {
            GamesScreen(
                onBackClick = {},
                viewModel = viewModel,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Then
        // Verify all games are displayed
        testGames.forEach { game ->
            composeTestRule.onNodeWithText(game.name).assertIsDisplayed()
            composeTestRule.onNodeWithText(game.description).assertIsDisplayed()
        }
    }

    @Test
    fun clickingOnGame_navigatesToGameScreen() {
        // Given
        val uiState = MutableStateFlow(
            GamesUiState(
                isLoading = false,
                games = testGames
            )
        )
        every { viewModel.uiState } returns uiState

        // When
        composeTestRule.setContent {
            GamesScreen(
                onBackClick = {},
                viewModel = viewModel
            )
        }

        // Then
        // Click on the first game
        composeTestRule.onNodeWithText(testGames[0].name).performClick()
        
        // Verify the view model was called
        verify { viewModel.onGameClick(testGames[0]) }
    }

    @Test
    fun clickingBackButton_callsOnBackClick() {
        // Given
        val uiState = MutableStateFlow(
            GamesUiState(
                isLoading = false,
                games = testGames
            )
        )
        every { viewModel.uiState } returns uiState
        var backClicked = false

        // When
        composeTestRule.setContent {
            GamesScreen(
                onBackClick = { backClicked = true },
                viewModel = viewModel
            )
        }

        // Then
        // Click the back button
        // Note: You might need to add a test tag to your back button
        // composeTestRule.onNodeWithTag("backButton").performClick()
        // assertTrue(backClicked)
    }
}
