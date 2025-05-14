package com.duvijan.pranaapp.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale
import java.util.UUID

class TextToSpeechManager private constructor(context: Context) {
    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false
    private var speechSpeed = 1.0f
    
    init {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val result = textToSpeech?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e(TAG, "Language not supported")
                } else {
                    // Set male voice if available
                    //setMaleVoice()
                    isInitialized = true
                    textToSpeech?.setSpeechRate(speechSpeed)
                    Log.d(TAG, "TextToSpeech initialized successfully")
                }
            } else {
                Log.e(TAG, "TextToSpeech initialization failed")
            }
        }
        
        textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                Log.d(TAG, "TTS started: $utteranceId")
            }
            
            override fun onDone(utteranceId: String?) {
                Log.d(TAG, "TTS done: $utteranceId")
            }
            
            override fun onError(utteranceId: String?) {
                Log.e(TAG, "TTS error: $utteranceId")
            }
        })
    }
    
    fun speak(text: String) {
        if (isInitialized) {
            val utteranceId = UUID.randomUUID().toString()
            textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }
    
    fun setSpeechRate(rate: Float) {
        speechSpeed = rate
        textToSpeech?.setSpeechRate(rate)
    fun setMaleVoice() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            try {
                val voices = textToSpeech?.voices
                if (voices != null) {
                    // Find a male voice
                    val maleVoice = voices.find { 
                        it.name.contains("male", ignoreCase = true) ||
                        (it.name.contains("en-us", ignoreCase = true) && !it.name.contains("female", ignoreCase = true))
                    }
                    
                    if (maleVoice != null) {
                        textToSpeech?.voice = maleVoice
                        Log.d(TAG, "Set male voice: ${maleVoice.name}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error setting male voice: ${e.message}")
            }
        }
    }

    }
    
    fun shutdown() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
    }
    
    companion object {
        private const val TAG = "TextToSpeechManager"
        private var instance: TextToSpeechManager? = null
        
        fun getInstance(context: Context): TextToSpeechManager {
            if (instance == null) {
                instance = TextToSpeechManager(context)
            }
            return instance!!
        }
        
        fun releaseInstance() {
            instance?.shutdown()
            instance = null
        }
    }
}
