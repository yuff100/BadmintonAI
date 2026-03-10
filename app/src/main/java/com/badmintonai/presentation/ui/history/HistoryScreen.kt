package com.badmintonai.presentation.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel()
) {
    var history by remember { mutableStateOf<List<com.badmintonai.domain.model.AnalysisResult>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        history = viewModel.getHistory()
    }
    
    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Analysis History") },
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
        if (history.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("No analysis history yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(history) { result ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { 
                            navController.navigate(
                                com.badmintonai.presentation.navigation.Screen.Results.createRoute(result.id)
                            )
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = result.strokeType.name.replace("_", " "),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${result.overallScore}/100",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = getScoreColor(result.overallScore)
                                )
                            }
                            Text(
                                text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                                    .format(Date(result.timestamp)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = result.summaryFeedback.take(100) + "...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun getScoreColor(score: Int): androidx.compose.ui.graphics.Color {
    return when {
        score >= 90 -> androidx.compose.ui.graphics.Color(0xFF4CAF50)
        score >= 70 -> androidx.compose.ui.graphics.Color(0xFFFFC107)
        else -> androidx.compose.ui.graphics.Color(0xFFF44336)
    }
}
