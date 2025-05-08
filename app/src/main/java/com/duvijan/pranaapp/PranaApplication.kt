package com.duvijan.pranaapp

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.duvijan.pranaapp.util.PeaceMeditationWorker
import com.google.firebase.FirebaseApp
import java.util.Calendar
import java.util.concurrent.TimeUnit

class PranaApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Schedule the Peace Meditation notification at 11:11 AM
        schedulePeaceMeditationReminder()
    }
    
    private fun schedulePeaceMeditationReminder() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val peaceMeditationWorkRequest = PeriodicWorkRequestBuilder<PeaceMeditationWorker>(
            24, TimeUnit.HOURS
        )
        .setConstraints(constraints)
        .build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "PeaceMeditationReminder",
            ExistingPeriodicWorkPolicy.KEEP,
            peaceMeditationWorkRequest
        )
    }
}
