package com.duvijan.pranaapp.ui.breathing

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeDown
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duvijan.pranaapp.R
import com.duvijan.pranaapp.model.BreathingStage
import com.duvijan.pranaapp.ui.components.CircularTimer
import com.duvijan.pranaapp.ui.components.BreathingPhaseIndicator
import com.duvijan.pranaapp.ui.components.VoiceControlSlider
import com.duvijan.pranaapp.ui.components.GradientButton
import com.duvijan.pranaapp.ui.components.BackgroundSoundSlider

@Composable
fun BreathingScreen(
    onPeaceMovementClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: BreathingViewModel = viewModel()
) {
    val context = LocalContext.current
    val inhaleDuration by viewModel.inhaleDuration.collectAsStateWithLifecycle()
    val holdDuration by viewModel.holdDuration.collectAsStateWithLifecycle()
    val exhaleDuration by viewModel.exhaleDuration.collectAsStateWithLifecycle()
    val silenceDuration by viewModel.silenceDuration.collectAsStateWithLifecycle()
    
    val isRunning by viewModel.isRunning.collectAsStateWithLifecycle()
    val currentStage by viewModel.currentStage.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()
    val currentCount by viewModel.currentCount.collectAsStateWithLifecycle()
    val totalCountInCycle by viewModel.totalCountInCycle.collectAsStateWithLifecycle()
    val cycleCount by viewModel.cycleCount.collectAsStateWithLifecycle()
    
    // Calculate progress for circular timer
    val totalDuration = when (currentStage) {
        BreathingStage.INHALE -> inhaleDuration.toIntOrNull() ?: 4
        BreathingStage.HOLD -> holdDuration.toIntOrNull() ?: 4
        BreathingStage.EXHALE -> exhaleDuration.toIntOrNull() ?: 4
        BreathingStage.SILENCE -> silenceDuration.toIntOrNull() ?: 4
    }
    val progress = 1f - (remainingSeconds.toFloat() / totalDuration.toFloat()).coerceIn(0f, 1f)
    
    // Voice control value
    var voiceControlValue by remember { mutableStateOf(0.5f) }
    
    // Background sound value
    var backgroundSoundValue by remember { mutableStateOf(0.5f) }
    
    // Initialize TTS when the screen is created
    LaunchedEffect(Unit) {
        viewModel.initializeTTS(context)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left - Settings icon
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_preferences),
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }
                
                // Center - App name or current screen title
                Text(
                    text = if (isRunning) "Timing" else "Sence",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                
                // Right - Menu icon
                IconButton(onClick = onPeaceMovementClick) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_more),
                        contentDescription = "Menu",
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (isRunning) {
                // Circular timer with current state
                CircularTimer(
                    currentStage = currentStage,
                    remainingSeconds = remainingSeconds,
                    currentCount = currentCount,
                    totalCountInCycle = totalCountInCycle,
                    cycleCount = cycleCount,
                    progress = progress,
                    modifier = Modifier.weight(1f)
                )
                
                // Breathing phase indicators
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    BreathingStage.values().forEach { stage ->
                        BreathingPhaseIndicator(
                            stage = stage,
                            isActive = stage == currentStage
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Voice control slider
                VoiceControlSlider(
                    value = voiceControlValue,
                    onValueChange = { 
                        voiceControlValue = it 
                        viewModel.setVoiceSpeed(0.5f + it)
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Background sound slider
                BackgroundSoundSlider(
                    value = backgroundSoundValue,
                    onValueChange = { 
                        backgroundSoundValue = it
                        viewModel.setBackgroundSoundVolume(it)
                    }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Stop button
                GradientButton(
                    text = stringResource(R.string.stop),
                    onClick = { viewModel.toggleTimer() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            } else {
                // Settings screen UI
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
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Start button
                GradientButton(
                    text = stringResource(R.string.start_practice),
                    onClick = { viewModel.toggleTimer() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )
            }
        }
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
            color = Color.White,
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
            color = Color.White,
            modifier = Modifier.width(80.dp)
        )
        
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(stringResource(R.string.duration_seconds)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = Color(0xFF4AEDB6),
                focusedBorderColor = Color(0xFF4AEDB6),
                unfocusedBorderColor = Color.Gray,
                focusedLabelColor = Color(0xFF4AEDB6),
                unfocusedLabelColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}
