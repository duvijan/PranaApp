package com.duvijan.pranaapp.ui.peace

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duvijan.pranaapp.R
import kotlinx.coroutines.delay

@Composable
fun PeaceMovementScreen(
    onBackClick: () -> Unit,
    viewModel: PeaceMovementViewModel = viewModel()
) {
    val peacekeeperCount by viewModel.peacekeeperCount.collectAsState()
    val isSignedUp by viewModel.isSignedUp.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var showMeditationTimer by remember { mutableStateOf(false) }
    var meditationTimeRemaining by remember { mutableStateOf(60) } // 1 minute
    
    // Handle meditation timer
    LaunchedEffect(showMeditationTimer) {
        if (showMeditationTimer) {
            viewModel.startPeaceMeditation()
            while (meditationTimeRemaining > 0) {
                delay(1000)
                meditationTimeRemaining--
            }
            viewModel.completePeaceMeditation()
            showMeditationTimer = false
            meditationTimeRemaining = 60
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.peace_movement),
                style = MaterialTheme.typography.headlineMedium
            )
            
            TextButton(onClick = onBackClick) {
                Text("Back")
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (showMeditationTimer) {
            // Show meditation timer
            MeditationTimer(meditationTimeRemaining)
        } else {
            // Show peace movement content
            PeaceMovementContent(
                peacekeeperCount = peacekeeperCount,
                isSignedUp = isSignedUp,
                isLoading = isLoading,
                onSignUpClick = { viewModel.signUpAsPeacekeeper() },
                onStartMeditationClick = { showMeditationTimer = true }
            )
        }
    }
}

@Composable
fun MeditationTimer(timeRemaining: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "#1minPeaceMeditation",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "$timeRemaining",
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Take a deep breath and focus on peace...",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PeaceMovementContent(
    peacekeeperCount: Int,
    isSignedUp: Boolean,
    isLoading: Boolean,
    onSignUpClick: () -> Unit,
    onStartMeditationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.peace_meditation_prompt),
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.peace_movement_count, peacekeeperCount),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (isSignedUp) {
            Text(
                text = stringResource(R.string.peace_movement_thanks),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onStartMeditationClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Text("Start #1minPeaceMeditation")
            }
        } else {
            Button(
                onClick = onSignUpClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(stringResource(R.string.i_am_peacekeeper))
                }
            }
        }
    }
}
