package com.duvijan.pranaapp.ui.breathing

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BreathingSettingsViewModel : ViewModel() {
    
    // Settings parameters
    private val _baseCount = MutableStateFlow(5)
    val baseCount: StateFlow<Int> = _baseCount
    
    private val _breathingCycles = MutableStateFlow(3)
    val breathingCycles: StateFlow<Int> = _breathingCycles
    
    private val _practiceDuration = MutableStateFlow(10)
    val practiceDuration: StateFlow<Int> = _practiceDuration
    
    private val _voiceGuidanceEnabled = MutableStateFlow(true)
    val voiceGuidanceEnabled: StateFlow<Boolean> = _voiceGuidanceEnabled
    
    private val _voiceSpeed = MutableStateFlow(1.0f)
    val voiceSpeed: StateFlow<Float> = _voiceSpeed
    
    fun updateBaseCount(count: Int) {
        if (count > 0) {
            _baseCount.value = count
        }
    }
    
    fun updateBreathingCycles(cycles: Int) {
        if (cycles > 0) {
            _breathingCycles.value = cycles
        }
    }
    
    fun updatePracticeDuration(minutes: Int) {
        if (minutes > 0) {
            _practiceDuration.value = minutes
        }
    }
    
    fun toggleVoiceGuidance() {
        _voiceGuidanceEnabled.value = !_voiceGuidanceEnabled.value
    }
    
    fun updateVoiceSpeed(speed: Float) {
        if (speed >= 0.5f && speed <= 2.0f) {
            _voiceSpeed.value = speed
        }
    }
    
    fun saveSettings(context: Context) {
        val sharedPreferences = context.getSharedPreferences("breathing_settings", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("base_count", _baseCount.value)
        editor.putInt("breathing_cycles", _breathingCycles.value)
        editor.putInt("practice_duration", _practiceDuration.value)
        editor.putBoolean("voice_guidance_enabled", _voiceGuidanceEnabled.value)
        editor.putFloat("voice_speed", _voiceSpeed.value)
        editor.apply()
    }
    
    fun loadSettings(context: Context) {
        val sharedPreferences = context.getSharedPreferences("breathing_settings", Context.MODE_PRIVATE)
        _baseCount.value = sharedPreferences.getInt("base_count", 5)
        _breathingCycles.value = sharedPreferences.getInt("breathing_cycles", 3)
        _practiceDuration.value = sharedPreferences.getInt("practice_duration", 10)
        _voiceGuidanceEnabled.value = sharedPreferences.getBoolean("voice_guidance_enabled", true)
        _voiceSpeed.value = sharedPreferences.getFloat("voice_speed", 1.0f)
    }
}
