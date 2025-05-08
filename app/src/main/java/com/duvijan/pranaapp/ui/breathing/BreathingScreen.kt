package com.duvijan.pranaapp.ui.breathing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.duvijan.pranaapp.R
import com.duvijan.pranaapp.model.BreathingStage

@Composable
fun BreathingScreen(
    onPeaceMovementClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: BreathingViewModel = viewModel()
) {
    val inhaleDuration by viewModel.inhaleDuration.collectAsState()
    val holdDuration by viewModel.holdDuration.collectAsState()
    val exhaleDuration by viewModel.exhaleDuration.collectAsState()
    val silenceDuration by viewModel.silenceDuration.collectAsState()
    
    val isRunning by viewModel.isRunning.collectAsState()
    val currentStage by viewModel.currentStage.collectAsState()
    val remainingSeconds by viewModel.remainingSeconds.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium
            )
            
            Row {
                TextButton(onClick = onPeaceMovementClick) {
                    Text(stringResource(R.string.peace_movement))
                }
                
                TextButton(onClick = onLogout) {
                    Text(stringResource(R.string.login))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Timer display
        if (isRunning) {
            TimerDisplay(currentStage, remainingSeconds)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Duration inputs
        if (!isRunning) {
            DurationInputs(
                inhaleDuration = inhaleDuration,
                holdDuration = holdDuration,
                exhaleDuration = exhaleDuration,
                silenceDuration = silenceDuration,
                onInhaleChange = viewModel::updateInhaleDuration,
                onHoldChange = viewModel::updateHoldDuration,
                onExhaleChange = viewModel::updateExhaleDuration,
                onSilenceChange = viewModel::updateSilenceDuration
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Start/Stop button
        Button(
            onClick = { viewModel.toggleTimer() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(56.dp)
        ) {
            Text(
                text = stringResource(if (isRunning) R.string.stop else R.string.start),
                fontSize = 18.sp
            )
        }
    }
}

@Composable
fun TimerDisplay(currentStage: BreathingStage, remainingSeconds: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.current_stage, currentStage.displayName),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.remaining_time, remainingSeconds),
            style = MaterialTheme.typography.displayLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DurationInputs(
    inhaleDuration: String,
    holdDuration: String,
    exhaleDuration: String,
    silenceDuration: String,
    onInhaleChange: (String) -> Unit,
    onHoldChange: (String) -> Unit,
    onExhaleChange: (String) -> Unit,
    onSilenceChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.breathing_timer),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        DurationInput(
            label = stringResource(R.string.inhale),
            value = inhaleDuration,
            onValueChange = onInhaleChange
        )
        
        DurationInput(
            label = stringResource(R.string.hold),
            value = holdDuration,
            onValueChange = onHoldChange
        )
        
        DurationInput(
            label = stringResource(R.string.exhale),
            value = exhaleDuration,
            onValueChange = onExhaleChange
        )
        
        DurationInput(
            label = stringResource(R.string.silence),
            value = silenceDuration,
            onValueChange = onSilenceChange
        )
    }
}

@Composable
fun DurationInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.width(80.dp)
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(stringResource(R.string.duration_seconds)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
