package com.badmintonai.presentation.ui.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
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

@Composable
fun ResultsScreen(
    navController: NavController,
    resultId: Long,
    viewModel: ResultsViewModel = hiltViewModel()
) {
    var result by remember { mutableStateOf<com.badmintonai.domain.model.AnalysisResult?>(null) }
    
    LaunchedEffect(resultId) {
        result = viewModel.getResult(resultId)
    }
    
    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Analysis Results") },
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
        result?.let { analysisResult ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Overall Score",
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "${analysisResult.overallScore}/100",
                                style = MaterialTheme.typography.displayLarge,
                                color = getScoreColor(analysisResult.overallScore)
                            )
                            Text(
                                text = analysisResult.strokeType.name.replace("_", " "),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                }
                
                items(analysisResult.dimensionScores) { dimensionScore ->
                    Card(
                        modifier = Modifier.fillMaxWidth()
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
                                    text = dimensionScore.dimension.name.replace("_", " "),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${dimensionScore.score}/100",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = getScoreColor(dimensionScore.score)
                                )
                            }
                            LinearProgressIndicator(
                                progress = dimensionScore.score / 100f,
                                modifier = Modifier.fillMaxWidth(),
                                color = getScoreColor(dimensionScore.score)
                            )
                            Text(
                                text = dimensionScore.feedback,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Summary",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = analysisResult.summaryFeedback,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        } ?: run {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Loading results...")
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
