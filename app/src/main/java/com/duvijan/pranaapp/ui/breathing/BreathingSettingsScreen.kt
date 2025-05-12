package com.duvijan.pranaapp.ui.breathing

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.duvijan.pranaapp.R

@Composable
fun BreathingSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: BreathingSettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val baseCount by viewModel.baseCount.collectAsStateWithLifecycle()
    val breathingCycles by viewModel.breathingCycles.collectAsStateWithLifecycle()
    val practiceDuration by viewModel.practiceDuration.collectAsStateWithLifecycle()
    val voiceGuidanceEnabled by viewModel.voiceGuidanceEnabled.collectAsStateWithLifecycle()
    val voiceSpeed by viewModel.voiceSpeed.collectAsStateWithLifecycle()
    
    var baseCountInput by remember { mutableStateOf(baseCount.toString()) }
    var breathingCyclesInput by remember { mutableStateOf(breathingCycles.toString()) }
    var practiceDurationInput by remember { mutableStateOf(practiceDuration.toString()) }
    
    LaunchedEffect(Unit) {
        viewModel.loadSettings(context)
        baseCountInput = baseCount.toString()
        breathingCyclesInput = breathingCycles.toString()
        practiceDurationInput = practiceDuration.toString()
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
            IconButton(onClick = onBackClick) {
                // Back icon
                Text("←", style = MaterialTheme.typography.headlineMedium)
            }
            
            Text(
                text = stringResource(R.string.breathing_settings),
                style = MaterialTheme.typography.headlineMedium
            )
            
            // Spacer for alignment
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Base Count Input
        OutlinedTextField(
            value = baseCountInput,
            onValueChange = { 
                baseCountInput = it
                it.toIntOrNull()?.let { value ->
                    viewModel.updateBaseCount(value)
                }
            },
            label = { Text(stringResource(R.string.base_count)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Breathing Cycles Input
        OutlinedTextField(
            value = breathingCyclesInput,
            onValueChange = { 
                breathingCyclesInput = it
                it.toIntOrNull()?.let { value ->
                    viewModel.updateBreathingCycles(value)
                }
            },
            label = { Text(stringResource(R.string.breathing_cycles)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Practice Duration Input
        OutlinedTextField(
            value = practiceDurationInput,
            onValueChange = { 
                practiceDurationInput = it
                it.toIntOrNull()?.let { value ->
                    viewModel.updatePracticeDuration(value)
                }
            },
            label = { Text(stringResource(R.string.practice_duration)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Voice Guidance Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.voice_guidance),
                style = MaterialTheme.typography.bodyLarge
            )
            
            Switch(
                checked = voiceGuidanceEnabled,
                onCheckedChange = { viewModel.toggleVoiceGuidance() }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Voice Speed Slider
        if (voiceGuidanceEnabled) {
            Text(
                text = stringResource(R.string.voice_speed),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Slider(
                value = voiceSpeed,
                onValueChange = { viewModel.updateVoiceSpeed(it) },
                valueRange = 0.5f..2.0f,
                steps = 5,
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.slow))
                Text(stringResource(R.string.normal))
                Text(stringResource(R.string.fast))
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Save Button
        Button(
            onClick = { 
                viewModel.saveSettings(context)
                onBackClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .height(56.dp)
        ) {
            Text(
                text = stringResource(R.string.save_settings),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
