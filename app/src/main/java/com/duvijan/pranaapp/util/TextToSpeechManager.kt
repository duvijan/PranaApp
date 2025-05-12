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
