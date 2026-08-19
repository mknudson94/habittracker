package com.mk.habittracker.nfccheckin

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.firebase.auth.FirebaseAuth
import com.mk.habittracker.core.data.HabitRepository
import com.mk.habittracker.core.nfc.HabitTrackerNdef
import com.mk.habittracker.core.nfc.NfcCheckInHandler
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import kotlin.random.Random
import com.mk.habittracker.core.ui.R as CoreR

internal const val NFC_UID_KEY = "nfc_uid"
internal const val NFC_HABIT_ID_KEY = "nfc_habit_id"
const val CHANNEL_ID = "nfc_checkin_notification_channel"
private const val NOTIFICATION_TIMEOUT = 12_000L

private const val ONE_DAY_STREAK_LENGTH = 1
private const val SHORT_STREAK_LENGTH = 2
private const val MEDIUM_STREAK_LENGTH = 7
private const val LONG_STREAK_LENGTH = 30

@HiltWorker
class NfcCheckInWorker
    @AssistedInject
    constructor(
        @Assisted val appContext: Context,
        @Assisted workerParams: WorkerParameters,
        private val nfcCheckInHandler: NfcCheckInHandler,
        private val habitRepository: HabitRepository,
        private val auth: FirebaseAuth,
    ) : CoroutineWorker(appContext, workerParams) {
        override suspend fun doWork(): Result {
            val tagId = this.inputData.getByteArray(NFC_UID_KEY)
            val habitId = this.inputData.getString(NFC_HABIT_ID_KEY) ?: error("null habit id")

            val habit =
                auth.currentUser?.let {
                    habitRepository.getHabit(it.uid, habitId).first()
                }!!
            val alreadyCheckedIn = habitRepository.hasCheckedInToday(habitId = habitId)
            val streakLength = auth.currentUser?.let {
                habitRepository.getCurrentStreak(it.uid, habitId)
            } ?: 0

            if (!alreadyCheckedIn) {
                nfcCheckInHandler.checkIn(
                    HabitTrackerNdef(
                        uid = tagId ?: byteArrayOf(),
                        habitId = habitId,
                    ),
                )
            }
            sendNotification(
                habitId = habitId,
                habitName = habit.name,
                alreadyCheckedIn = alreadyCheckedIn,
                streakLength = streakLength,
            )
            return Result.success()
        }

        private fun sendNotification(
            habitId: String,
            habitName: String,
            alreadyCheckedIn: Boolean,
            streakLength: Int,
        ) {
            Log.d("worker", "sending notification")
            val (textTitle, textContent) =
                getCheckInStringsFor(
                    habitName = habitName,
                    alreadyCheckedIn = alreadyCheckedIn,
                    streakLength = streakLength,
                )
            val notificationId = Random.nextInt()
            val contentIntent = buildCheckInPendingIntent(notificationId, habitId)

            val builder =
                NotificationCompat
                    .Builder(appContext, CHANNEL_ID)
                    .setContentTitle(textTitle)
                    .setContentText(textContent)
                    .setSmallIcon(CoreR.drawable.priority_16dp)
                    .setContentIntent(contentIntent)
                    // dismisses on-tap
                    .setAutoCancel(true)
                    // dismiss automatically after short delay
                    .setTimeoutAfter(NOTIFICATION_TIMEOUT)
                    .apply {
                        if (!alreadyCheckedIn) {
                            val undoIntent = buildUndoPendingIntent(notificationId, habitId)
                            addAction(
                                R.drawable.undo,
                                appContext.getString(R.string.undo),
                                undoIntent,
                            )
                        }
                    }

            with(NotificationManagerCompat.from(appContext)) {
                if (ActivityCompat.checkSelfPermission(
                        appContext,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // TODO: Consider calling ActivityCompat#requestPermissions here
                    // https://developer.android.com/develop/ui/compose/notifications/create-notification#notify
                    Log.w("worker", "POST_NOTIFICATIONS permission not granted")
                    return@with
                }
                // Save id somewhere if I ever want to update/delete notifications programatically
                Log.d("worker", "actually calling notify for $notificationId")
                notify(notificationId, builder.build())
            }
        }

        fun buildCheckInPendingIntent(
            notificationId: Int,
            habitId: String,
        ): PendingIntent {
            // TODO: move uri string to core:nav or something
            val intent =
                Intent(Intent.ACTION_VIEW, "com.mk.habittracker://habit/$habitId".toUri())
                    .apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(EXTRA_HABIT_ID, habitId)
                        putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                    }

            return PendingIntent.getActivity(
                appContext,
                habitId.hashCode(),
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }

        fun buildUndoPendingIntent(
            notificationId: Int,
            habitId: String,
        ): PendingIntent {
            // PendingIntent.getBroadcast for the action button
            val undoIntent =
                Intent(appContext, UndoCheckInReceiver::class.java)
                    .apply {
                        action = ACTION_UNDO_CHECKIN
                        putExtra(EXTRA_HABIT_ID, habitId)
                        putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                    }

            return PendingIntent.getBroadcast(
                appContext,
                habitId.hashCode(),
                undoIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
        }

        private fun getCheckInStringsFor(
            habitName: String,
            alreadyCheckedIn: Boolean,
            streakLength: Int,
        ): Pair<String, String> {
            if (alreadyCheckedIn) {
                return habitName to appContext.getString(R.string.already_logged_body)
            }

            val title = appContext.getString(R.string.check_in_notification_title, habitName)
            val body =
                when {
                    streakLength >= LONG_STREAK_LENGTH -> appContext.getString(
                        R.string.streak_day_x,
                        streakLength,
                    )
                    streakLength >= MEDIUM_STREAK_LENGTH ->
                        appContext.getString(
                            R.string.streak_becoming_habit,
                            streakLength,
                        )
                    streakLength >= SHORT_STREAK_LENGTH ->
                        appContext.getString(
                            R.string.streak_in_a_row,
                            streakLength,
                        )
                    streakLength == ONE_DAY_STREAK_LENGTH -> appContext.getString(
                        R.string.streak_first_one,
                    )
                    else -> error("streak length should be greater than zero, was $streakLength")
                }
            return title to body
        }
    }
