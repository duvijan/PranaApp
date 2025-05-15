package com.duvijan.pranaapp.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.duvijan.pranaapp.R

class AudioManager private constructor(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false
    private var volume = 0.5f
    
    init {
        initializeMediaPlayer()
    }
    
    private fun initializeMediaPlayer() {
        try {
            mediaPlayer = MediaPlayer.create(context, R.raw.calming_flute)
            mediaPlayer?.isLooping = true
            mediaPlayer?.setVolume(volume, volume)
            Log.d(TAG, "MediaPlayer initialized successfully with calming flute sound")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MediaPlayer: ${e.message}")
        }
    }
    
    fun startBackgroundSound() {
        try {
            if (mediaPlayer == null) {
                initializeMediaPlayer()
            }
            
            if (!isPlaying) {
                mediaPlayer?.start()
                isPlaying = true
                Log.d(TAG, "Background sound started")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error starting background sound: ${e.message}")
        }
    }
    
    fun pauseBackgroundSound() {
        try {
            if (isPlaying) {
                mediaPlayer?.pause()
                isPlaying = false
                Log.d(TAG, "Background sound paused")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error pausing background sound: ${e.message}")
        }
    }
    
    fun setVolume(volumeLevel: Float) {
        volume = volumeLevel
        mediaPlayer?.setVolume(volume, volume)
        Log.d(TAG, "Volume set to $volume")
    }
    
    fun release() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        Log.d(TAG, "MediaPlayer released")
    }
    
    companion object {
        private const val TAG = "AudioManager"
        private var instance: AudioManager? = null
        
        fun getInstance(context: Context): AudioManager {
            if (instance == null) {
                instance = AudioManager(context)
            }
            return instance!!
        }
        
        fun releaseInstance() {
            instance?.release()
            instance = null
        }
    }
}
