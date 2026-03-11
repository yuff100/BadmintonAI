package com.badmintonai.presentation.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.badmintonai.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Badminton AI Coach") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Improve Your Badminton Skills",
                style = MaterialTheme.typography.headlineMedium
            )
            
            Button(
                onClick = { navController.navigate(Screen.Recording.route) },
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text("Start Analysis", modifier = Modifier.padding(8.dp))
            }
            
            Button(
                onClick = { navController.navigate(Screen.History.route) },
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text("View History", modifier = Modifier.padding(8.dp))
            }
        }
    }
}
