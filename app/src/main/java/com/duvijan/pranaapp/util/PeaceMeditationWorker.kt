package com.duvijan.pranaapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.duvijan.pranaapp.MainActivity
import com.duvijan.pranaapp.R
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class PeaceMeditationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    private val firebaseAnalytics: FirebaseAnalytics = Firebase.analytics

    override fun doWork(): Result {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)

        // Check if it's 11:11 AM
        if (currentHour == 11 && currentMinute == 11) {
            // Log event to Firebase Analytics
            firebaseAnalytics.logEvent("peace_meditation_notification", null)
            
            // Show notification
            showPeaceMeditationNotification()
        }

        return Result.success()
    }

    private fun showPeaceMeditationNotification() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Peace Meditation",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for Peace Meditation"
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        // Create intent for when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(PEACE_MEDITATION_EXTRA, true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Build notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_peace_notification)
            .setContentTitle(context.getString(R.string.peace_movement))
            .setContentText(context.getString(R.string.peace_meditation_prompt))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        // Show notification
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    companion object {
        private const val CHANNEL_ID = "peace_meditation_channel"
        private const val NOTIFICATION_ID = 1111
        const val PEACE_MEDITATION_EXTRA = "peace_meditation_extra"
    }
}
