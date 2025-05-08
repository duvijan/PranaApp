package com.duvijan.pranaapp.util

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import android.os.Bundle

object AnalyticsManager {
    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics
    
    // User authentication events
    fun logLogin(method: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.LOGIN) {
            param(FirebaseAnalytics.Param.METHOD, method)
        }
    }
    
    fun logSignUp(method: String) {
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SIGN_UP) {
            param(FirebaseAnalytics.Param.METHOD, method)
        }
    }
    
    fun logLogout() {
        firebaseAnalytics.logEvent("logout", null)
    }
    
    // Timer events
    fun logTimerStart(inhale: Int, hold: Int, exhale: Int, silence: Int) {
        firebaseAnalytics.logEvent("timer_start") {
            param("inhale_duration", inhale.toLong())
            param("hold_duration", hold.toLong())
            param("exhale_duration", exhale.toLong())
            param("silence_duration", silence.toLong())
        }
    }
    
    fun logTimerStop(elapsedTime: Int) {
        firebaseAnalytics.logEvent("timer_stop") {
            param("elapsed_time", elapsedTime.toLong())
        }
    }
    
    fun logStageChange(stage: String) {
        firebaseAnalytics.logEvent("stage_change") {
            param("stage", stage)
        }
    }
    
    // Peace movement events
    fun logPeaceMovementSignup() {
        firebaseAnalytics.logEvent("peace_movement_signup", null)
    }
    
    fun logPeaceMeditationStart() {
        firebaseAnalytics.logEvent("peace_meditation_start", null)
    }
    
    fun logPeaceMeditationComplete() {
        firebaseAnalytics.logEvent("peace_meditation_complete", null)
    }
}
