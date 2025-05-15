package com.duvijan.pranaapp.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.duvijan.pranaapp.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CustomVoiceManager private constructor(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var isInitialized = false
    private var countingJob: Job? = null
    private var volume = 0.7f
    
    // Map of number audio resources
    private val numberAudioMap = mapOf(
        1 to R.raw.number_1,
        2 to R.raw.number_2,
        3 to R.raw.number_3,
        4 to R.raw.number_4,
        5 to R.raw.number_5,
        6 to R.raw.number_6,
        7 to R.raw.number_7,
        8 to R.raw.number_8,
        9 to R.raw.number_9,
        10 to R.raw.number_10
    )
    
    // Map of phase announcement audio resources
    private val phaseAudioMap = mapOf(
        "inhale" to R.raw.inhale_voice,
        "hold" to R.raw.hold_voice,
        "exhale" to R.raw.exhale_voice,
        "silence" to R.raw.silence_voice
    )
    
    init {
        try {
            isInitialized = true
            Log.d(TAG, "CustomVoiceManager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing CustomVoiceManager: ${e.message}")
        }
    }
    
    fun playPhaseAnnouncement(phase: String) {
        if (!isInitialized) return
        
        try {
            val resId = phaseAudioMap[phase.lowercase()] ?: return
            playAudioResource(resId)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing phase announcement: ${e.message}")
        }
    }
    
    fun playNumber(number: Int) {
        if (!isInitialized) return
        
        try {
            val resId = numberAudioMap[number] ?: return
            playAudioResource(resId)
        } catch (e: Exception) {
            Log.e(TAG, "Error playing number: ${e.message}")
        }
    }
    
    fun countSequentially(from: Int, to: Int, delayMs: Long, onComplete: () -> Unit) {
        countingJob?.cancel()
        
        countingJob = CoroutineScope(Dispatchers.Main).launch {
            for (i in from..to) {
                playNumber(i)
                delay(delayMs)
            }
            onComplete()
        }
    }
    
    private fun playAudioResource(resId: Int) {
        try {
            // Release previous MediaPlayer if any
            releaseMediaPlayer()
            
            // Create and start new MediaPlayer
            mediaPlayer = MediaPlayer.create(context, resId)
            mediaPlayer?.setVolume(volume, volume)
            mediaPlayer?.setOnCompletionListener { mp ->
                mp.release()
                mediaPlayer = null
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing audio resource: ${e.message}")
        }
    }
    
    private fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
    
    fun setVolume(volumeLevel: Float) {
        volume = volumeLevel
        mediaPlayer?.setVolume(volume, volume)
    }
    
    fun release() {
        countingJob?.cancel()
        countingJob = null
        releaseMediaPlayer()
        isInitialized = false
        Log.d(TAG, "CustomVoiceManager released")
    }
    
    companion object {
        private const val TAG = "CustomVoiceManager"
        private var instance: CustomVoiceManager? = null
        
        fun getInstance(context: Context): CustomVoiceManager {
            if (instance == null) {
                instance = CustomVoiceManager(context)
            }
            return instance!!
        }
        
        fun releaseInstance() {
            instance?.release()
            instance = null
        }
    }
}
