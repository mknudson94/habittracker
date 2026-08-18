package com.mk.habittracker

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.mk.habittracker.nfccheckin.CHANNEL_ID
import com.mk.habittracker.nfccheckin.R
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HabitTrackerApp :
    Application(),
    Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createCheckInNotificationChannel(this)
    }

    override val workManagerConfiguration: Configuration =
        Configuration
            .Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun createCheckInNotificationChannel(context: Context) {
        Log.d("HabitTrackerApp", "Creating notification channel")
        val name = context.getString(com.mk.habittracker.nfccheckin.R.string.channel_name)
        val descriptionText = context.getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel =
            NotificationChannel(
                // id =
                CHANNEL_ID,
                // name =
                name,
                // importance =
                importance,
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
