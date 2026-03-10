package com.badmintonai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.badmintonai.presentation.ui.analysis.AnalysisScreen
import com.badmintonai.presentation.ui.home.HomeScreen
import com.badmintonai.presentation.ui.history.HistoryScreen
import com.badmintonai.presentation.ui.recording.RecordingScreen
import com.badmintonai.presentation.ui.results.ResultsScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Recording : Screen("recording")
    object Analysis : Screen("analysis/{videoPath}") {
        fun createRoute(videoPath: String) = "analysis/$videoPath"
    }
    object Results : Screen("results/{resultId}") {
        fun createRoute(resultId: Long) = "results/$resultId"
    }
    object History : Screen("history")
}

@Composable
fun AppNavigation(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(Screen.Recording.route) {
            RecordingScreen(navController = navController)
        }
        
        composable(Screen.Analysis.route) { backStackEntry ->
            val videoPath = backStackEntry.arguments?.getString("videoPath") ?: ""
            AnalysisScreen(
                navController = navController,
                videoPath = videoPath
            )
        }
        
        composable(Screen.Results.route) { backStackEntry ->
            val resultId = backStackEntry.arguments?.getLong("resultId") ?: 0L
            ResultsScreen(
                navController = navController,
                resultId = resultId
            )
        }
        
        composable(Screen.History.route) {
            HistoryScreen(navController = navController)
        }
    }
}
