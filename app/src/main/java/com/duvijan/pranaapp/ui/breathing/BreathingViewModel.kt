package com.duvijan.pranaapp.ui.breathing

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duvijan.pranaapp.model.BreathingStage
import com.duvijan.pranaapp.util.AnalyticsManager
import com.duvijan.pranaapp.util.AudioManager
import com.duvijan.pranaapp.util.TextToSpeechManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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
    
    // Voice-guided breathing parameters
    private val _currentCount = MutableStateFlow(0)
    val currentCount: StateFlow<Int> = _currentCount
    
    private val _totalCountInCycle = MutableStateFlow(0)
    val totalCountInCycle: StateFlow<Int> = _totalCountInCycle
    
    private val _cycleCount = MutableStateFlow(0)
    val cycleCount: StateFlow<Int> = _cycleCount
    
    private var timerJob: Job? = null
    private var countingJob: Job? = null
    private var elapsedTime = 0
    private var startTime = 0L
    private var endTime = 0L
    
    // TTS and settings
    private var ttsManager: TextToSpeechManager? = null
    private var audioManager: AudioManager? = null
    private var baseCount = 5
    private var breathingCycles = 3
    private var practiceDuration = 10
    private var voiceGuidanceEnabled = true
    private var voiceSpeed = 1.0f
    
    fun initializeTTS(context: Context) {
        ttsManager = TextToSpeechManager.getInstance(context)
        audioManager = AudioManager.getInstance(context)
        loadSettings(context)
    }
    
    private fun loadSettings(context: Context) {
        val sharedPreferences = context.getSharedPreferences("breathing_settings", Context.MODE_PRIVATE)
        baseCount = sharedPreferences.getInt("base_count", 5)
        breathingCycles = sharedPreferences.getInt("breathing_cycles", 3)
        practiceDuration = sharedPreferences.getInt("practice_duration", 10)
        voiceGuidanceEnabled = sharedPreferences.getBoolean("voice_guidance_enabled", true)
        voiceSpeed = sharedPreferences.getFloat("voice_speed", 1.0f)
        
        if (ttsManager != null) {
            ttsManager?.setSpeechRate(voiceSpeed)
        }
        
        _totalCountInCycle.value = baseCount * 4 // 4 stages
    }
    
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
    
    fun setVoiceSpeed(speed: Float) {
        voiceSpeed = speed
        ttsManager?.setSpeechRate(speed)
    }
    
    fun setBackgroundSoundVolume(volume: Float) {
        audioManager?.setVolume(volume)
    }
    
    fun toggleTimer() {
        if (_isRunning.value) {
            stopTimer()
            audioManager?.pauseBackgroundSound()
        } else {
            startTimer()
            audioManager?.startBackgroundSound()
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
        
        // Initialize voice-guided counting parameters
        _currentCount.value = 1
        _cycleCount.value = 1
        startTime = System.currentTimeMillis()
        endTime = startTime + TimeUnit.MINUTES.toMillis(practiceDuration.toLong())
        
        _isRunning.value = true
        elapsedTime = 0
        _currentStage.value = BreathingStage.INHALE
        _remainingSeconds.value = inhale
        
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000) // 1 second delay
                elapsedTime++
                
                _remainingSeconds.value = _remainingSeconds.value - 1
                
                // Check if practice duration has elapsed
                if (System.currentTimeMillis() > endTime) {
                    stopTimer()
                    break
                }
                
                if (_remainingSeconds.value <= 0) {
                    moveToNextStage()
                }
            }
        }
        
        // Start voice counting if enabled
        if (voiceGuidanceEnabled) {
            startVoiceCounting()
        }
    }
    
    private fun startVoiceCounting() {
        countingJob = viewModelScope.launch {
            while (_isRunning.value && System.currentTimeMillis() < endTime) {
                // Announce the breathing phase first
                val phaseAnnouncement = when (_currentStage.value) {
                    BreathingStage.INHALE -> "Inhale"
                    BreathingStage.HOLD -> "Hold"
                    BreathingStage.EXHALE -> "Exhale"
                    BreathingStage.SILENCE -> "Silence"
                }
                ttsManager?.speak(phaseAnnouncement)
                
                // Short delay after announcing the phase
                delay(500)
                
                // Then speak the count
                ttsManager?.speak(_currentCount.value.toString())
                
                // Calculate delay based on current stage duration
                val stageDuration = when (_currentStage.value) {
                    BreathingStage.INHALE -> _inhaleDuration.value.toIntOrNull() ?: 4
                    BreathingStage.HOLD -> _holdDuration.value.toIntOrNull() ?: 4
                    BreathingStage.EXHALE -> _exhaleDuration.value.toIntOrNull() ?: 4
                    BreathingStage.SILENCE -> _silenceDuration.value.toIntOrNull() ?: 4
                }
                
                // Calculate delay based on counts per stage
                val countsPerStage = baseCount
                val delayMillis = (stageDuration * 1000) / countsPerStage
                
                delay(delayMillis.toLong())
                
                _currentCount.value += 1
                if (_currentCount.value > _totalCountInCycle.value) {
                    _currentCount.value = 1
                    _cycleCount.value += 1
                    
                    // Check if we've completed all cycles
                    if (_cycleCount.value > breathingCycles) {
                        break
                    }
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
        
        // If we've completed a full cycle (back to INHALE)
        if (nextStage == BreathingStage.INHALE && _cycleCount.value >= breathingCycles) {
            // Check if we've reached the maximum number of cycles
            stopTimer()
        }
    }
    
    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        countingJob?.cancel()
        countingJob = null
        _isRunning.value = false
        
        // Log analytics event
        AnalyticsManager.logTimerStop(elapsedTime)
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        countingJob?.cancel()
        ttsManager = null
        TextToSpeechManager.releaseInstance()
        AudioManager.releaseInstance()
    }
}
