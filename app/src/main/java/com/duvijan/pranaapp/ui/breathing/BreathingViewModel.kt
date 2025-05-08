package com.duvijan.pranaapp.ui.breathing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duvijan.pranaapp.model.BreathingStage
import com.duvijan.pranaapp.util.AnalyticsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BreathingViewModel : ViewModel() {
    
    // Duration inputs for each stage
    private val _inhaleDuration = MutableStateFlow("4")
    val inhaleDuration: StateFlow<String> = _inhaleDuration
    
    private val _holdDuration = MutableStateFlow("4")
    val holdDuration: StateFlow<String> = _holdDuration
    
    private val _exhaleDuration = MutableStateFlow("4")
    val exhaleDuration: StateFlow<String> = _exhaleDuration
    
    private val _silenceDuration = MutableStateFlow("4")
    val silenceDuration: StateFlow<String> = _silenceDuration
    
    // Timer state
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning
    
    private val _currentStage = MutableStateFlow(BreathingStage.INHALE)
    val currentStage: StateFlow<BreathingStage> = _currentStage
    
    private val _remainingSeconds = MutableStateFlow(0)
    val remainingSeconds: StateFlow<Int> = _remainingSeconds
    
    private var timerJob: Job? = null
    private var elapsedTime = 0
    
    fun updateInhaleDuration(duration: String) {
        if (duration.isEmpty() || duration.matches(Regex("^\\d+$"))) {
            _inhaleDuration.value = duration
        }
    }
    
    fun updateHoldDuration(duration: String) {
        if (duration.isEmpty() || duration.matches(Regex("^\\d+$"))) {
            _holdDuration.value = duration
        }
    }
    
    fun updateExhaleDuration(duration: String) {
        if (duration.isEmpty() || duration.matches(Regex("^\\d+$"))) {
            _exhaleDuration.value = duration
        }
    }
    
    fun updateSilenceDuration(duration: String) {
        if (duration.isEmpty() || duration.matches(Regex("^\\d+$"))) {
            _silenceDuration.value = duration
        }
    }
    
    fun toggleTimer() {
        if (_isRunning.value) {
            stopTimer()
        } else {
            startTimer()
        }
    }
    
    private fun startTimer() {
        if (timerJob != null) return
        
        // Validate inputs
        val inhale = _inhaleDuration.value.toIntOrNull() ?: 0
        val hold = _holdDuration.value.toIntOrNull() ?: 0
        val exhale = _exhaleDuration.value.toIntOrNull() ?: 0
        val silence = _silenceDuration.value.toIntOrNull() ?: 0
        
        if (inhale <= 0 || hold <= 0 || exhale <= 0 || silence <= 0) {
            return
        }
        
        // Log analytics event
        AnalyticsManager.logTimerStart(inhale, hold, exhale, silence)
        
        _isRunning.value = true
        elapsedTime = 0
        _currentStage.value = BreathingStage.INHALE
        _remainingSeconds.value = inhale
        
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000) // 1 second delay
                elapsedTime++
                
                _remainingSeconds.value = _remainingSeconds.value - 1
                
                if (_remainingSeconds.value <= 0) {
                    moveToNextStage()
                }
            }
        }
    }
    
    private fun moveToNextStage() {
        val nextStage = when (_currentStage.value) {
            BreathingStage.INHALE -> BreathingStage.HOLD
            BreathingStage.HOLD -> BreathingStage.EXHALE
            BreathingStage.EXHALE -> BreathingStage.SILENCE
            BreathingStage.SILENCE -> BreathingStage.INHALE
        }
        
        _currentStage.value = nextStage
        AnalyticsManager.logStageChange(nextStage.displayName)
        
        _remainingSeconds.value = when (nextStage) {
            BreathingStage.INHALE -> _inhaleDuration.value.toIntOrNull() ?: 4
            BreathingStage.HOLD -> _holdDuration.value.toIntOrNull() ?: 4
            BreathingStage.EXHALE -> _exhaleDuration.value.toIntOrNull() ?: 4
            BreathingStage.SILENCE -> _silenceDuration.value.toIntOrNull() ?: 4
        }
    }
    
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _isRunning.value = false
        
        // Log analytics event
        AnalyticsManager.logTimerStop(elapsedTime)
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
