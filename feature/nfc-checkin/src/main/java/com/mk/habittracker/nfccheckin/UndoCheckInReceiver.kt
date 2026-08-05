package com.mk.habittracker.nfccheckin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.mk.habittracker.core.data.HabitRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

internal const val ACTION_UNDO_CHECKIN = "com.mk.habittracker.ACTION_UNDO_CHECKIN"
internal const val EXTRA_HABIT_ID = "extra_habit_id"
internal const val EXTRA_NOTIFICATION_ID = "extra_notification_id"

@AndroidEntryPoint
class UndoCheckInReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: HabitRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_UNDO_CHECKIN) return

        val habitId = intent.getStringExtra(EXTRA_HABIT_ID) ?: return
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val userId = Firebase.auth.currentUser?.uid ?: "anonymous"

        // Cancel the notification immediately
        if (notificationId != -1) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }

        // Perform undo in background
        CoroutineScope(Dispatchers.IO).launch {
            repository.deleteCheckIn(
                habitId = habitId,
                date = LocalDate.now(),
                userId = userId
            )
        }
    }
}
