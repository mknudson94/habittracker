package com.mk.habittracker.nfccheckin

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mk.habittracker.core.nfc.HabitTrackerNdef
import com.mk.habittracker.core.nfc.NfcCheckInHandler
import dagger.assisted.Assisted
import javax.inject.Inject
import kotlin.random.Random

internal const val NFC_UID_KEY = "nfc_uid"
internal const val NFC_HABIT_ID_KEY = "nfc_habit_id"
const val CHANNEL_ID = "nfc_checkin_notification_channel"

@HiltWorker
class NfcCheckInWorker @Inject constructor(
    @Assisted val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val nfcCheckInHandler: NfcCheckInHandler,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val tagId = this.inputData.getByteArray(NFC_UID_KEY)
        val habitId = this.inputData.getString(NFC_HABIT_ID_KEY) ?: error("null habit id")

        nfcCheckInHandler.checkIn(
            HabitTrackerNdef(
                uid = tagId ?: byteArrayOf(),
                habitId = habitId
            )
        )

        sendNotification(habitId)

        return Result.success()
    }

    private fun sendNotification(habitId: String) {
        val textTitle = appContext.getString(R.string.check_in_notification_title)
        val textContent = appContext.getString(R.string.check_in_notification_content)
        val notificationId = Random.nextInt()
        val contentIntent = buildCheckInPendingIntent(notificationId, habitId)
        val undoIntent = buildUndoPendingIntent(notificationId, habitId)

        val builder = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setContentTitle(textTitle)
            .setContentText(textContent)
            .setContentIntent(contentIntent)
            .addAction(R.drawable.undo, appContext.getString(R.string.undo), undoIntent)
            .setAutoCancel(true) // dismisses on-tap
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(appContext)) {
            if (ActivityCompat.checkSelfPermission(
                    appContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling ActivityCompat#requestPermissions here
                // https://developer.android.com/develop/ui/compose/notifications/create-notification#notify
                return@with
            }
            // Save id somewhere if I ever want to update/delete notifications programatically
            notify(notificationId, builder.build())
        }
    }

    fun buildCheckInPendingIntent(notificationId: Int, habitId: String): PendingIntent {
        // TODO: move uri string to core:nav or something
        val intent = Intent(Intent.ACTION_VIEW, "com.mk.habittracker://habit/$habitId".toUri())
            .apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_HABIT_ID, habitId)
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }

        return PendingIntent.getActivity(
            appContext,
            habitId.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    fun buildUndoPendingIntent(notificationId: Int, habitId: String): PendingIntent {
        // PendingIntent.getBroadcast for the action button
        val undoIntent = Intent(appContext, UndoCheckInReceiver::class.java)
            .apply {
                action = ACTION_UNDO_CHECKIN
                putExtra(EXTRA_HABIT_ID, habitId)
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }

        return PendingIntent.getBroadcast(
            appContext,
            habitId.hashCode(),
            undoIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
    }
}
