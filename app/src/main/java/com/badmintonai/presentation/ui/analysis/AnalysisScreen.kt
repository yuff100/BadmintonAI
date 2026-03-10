package com.badmintonai.presentation.ui.analysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.badmintonai.presentation.navigation.Screen

@Composable
fun AnalysisScreen(
    navController: NavController,
    videoPath: String,
    viewModel: AnalysisViewModel = hiltViewModel()
) {
    var analysisResult by remember { mutableStateOf<Long?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(videoPath) {
        viewModel.analyzeVideo(videoPath).collect { result ->
            result.onSuccess { resultId ->
                analysisResult = resultId
                navController.navigate(Screen.Results.createRoute(resultId)) {
                    popUpTo(Screen.Recording.route) { inclusive = true }
                }
            }.onFailure { error ->
                errorMessage = error.message
                isLoading = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Analyzing Motion") },
                navigationIcon = {
                    androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) {
                        androidx.compose.material3.Icon(
                            imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            spaceBy = 24.dp
        ) {
            if (isLoading) {
                CircularProgressIndicator()
                Text(
                    text = "Analyzing your swing...",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Please wait, this may take a few seconds",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            errorMessage?.let { message ->
                Text(
                    text = "Error: $message",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Button(onClick = { navController.popBackStack() }) {
                    Text("Try Again")
                }
            }
        }
    }
}
