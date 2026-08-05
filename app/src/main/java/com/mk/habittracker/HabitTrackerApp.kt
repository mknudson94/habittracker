package com.mk.habittracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import com.mk.habittracker.nfccheckin.CHANNEL_ID
import com.mk.habittracker.nfccheckin.R
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class HabitTrackerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        createCheckInNotificationChannel(this)
    }

    private fun createCheckInNotificationChannel(context: Context) {
        val name = context.getString(com.mk.habittracker.nfccheckin.R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(
            /* id = */ CHANNEL_ID,
            /* name = */ name,
            /* importance = */ importance,
        ).apply {
            description = descriptionText
            enableVibration(false) // your custom haptic already covers this
        }
        // Register the channel with the system.
        val notificationManager: NotificationManager =
            context.getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}
