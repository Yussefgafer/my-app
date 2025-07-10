package com.chatex.app.presentation.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_showsWelcomeMessage() {
        // When
        composeTestRule.setContent {
            HomeScreen(
                onNavigateToAiChat = {},
                onNavigateToGames = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Welcome to ChateX").assertIsDisplayed()
    }

    @Test
    fun homeScreen_showsAiChatCard() {
        // When
        composeTestRule.setContent {
            HomeScreen(
                onNavigateToAiChat = {},
                onNavigateToGames = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("AI Chat").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chat with our AI assistant").assertIsDisplayed()
    }

    @Test
    fun homeScreen_showsGamesCard() {
        // When
        composeTestRule.setContent {
            HomeScreen(
                onNavigateToAiChat = {},
                onNavigateToGames = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Games").assertIsDisplayed()
        composeTestRule.onNodeWithText("Play fun games with friends").assertIsDisplayed()
    }

    @Test
    fun clickingAiChatCard_callsOnNavigateToAiChat() {
        // Given
        var aiChatClicked = false

        // When
        composeTestRule.setContent {
            HomeScreen(
                onNavigateToAiChat = { aiChatClicked = true },
                onNavigateToGames = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("AI Chat").performClick()
        assert(aiChatClicked)
    }

    @Test
    fun clickingGamesCard_callsOnNavigateToGames() {
        // Given
        var gamesClicked = false

        // When
        composeTestRule.setContent {
            HomeScreen(
                onNavigateToAiChat = {},
                onNavigateToGames = { gamesClicked = true }
            )
        }

        // Then
        composeTestRule.onNodeWithText("Games").performClick()
        assert(gamesClicked)
    }
}
