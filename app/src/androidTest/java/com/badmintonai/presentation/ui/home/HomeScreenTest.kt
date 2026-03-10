package com.badmintonai.presentation.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.badmintonai.presentation.theme.BadmintonAITheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun homeScreen_displaysCorrectTitle() {
        composeTestRule.setContent {
            BadmintonAITheme {
                HomeScreen(navController = mock())
            }
        }

        composeTestRule.onNodeWithText("Badminton AI Coach").assertIsDisplayed()
    }

    @Test
    fun homeScreen_displaysAllButtons() {
        composeTestRule.setContent {
            BadmintonAITheme {
                HomeScreen(navController = mock())
            }
        }

        composeTestRule.onNodeWithText("Start Analysis").assertIsDisplayed()
        composeTestRule.onNodeWithText("View History").assertIsDisplayed()
    }

    @Test
    fun startAnalysisButton_click_navigatesToRecording() {
        val navController = mock<androidx.navigation.NavController>()
        
        composeTestRule.setContent {
            BadmintonAITheme {
                HomeScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("Start Analysis").performClick()
        verify(navController).navigate("recording")
    }

    @Test
    fun viewHistoryButton_click_navigatesToHistory() {
        val navController = mock<androidx.navigation.NavController>()
        
        composeTestRule.setContent {
            BadmintonAITheme {
                HomeScreen(navController = navController)
            }
        }

        composeTestRule.onNodeWithText("View History").performClick()
        verify(navController).navigate("history")
    }
}
