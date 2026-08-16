package com.mk.habittracker.nfccheckin

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import com.mk.habittracker.core.nfc.parseHabitTrackerNdef

class NfcCheckInTrampolineActivity : ComponentActivity() {
    private val workManager by lazy { WorkManager.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("intent", "onCreate trampoline")
        handleIntent(intent)
        finish()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.d("intent", "onNewIntent trampoline")
        setIntent(intent)
        handleIntent(intent)
        finish()
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        Log.d("intent", "handling intent from trampoline: $intent")

        val ndef = intent.parseHabitTrackerNdef() ?: return // TODO: handle error here
        val inputData =
            Data
                .Builder()
                .putByteArray(NFC_UID_KEY, ndef.uid)
                .putString(NFC_HABIT_ID_KEY, ndef.habitId)
                .build()

        val workRequest =
            OneTimeWorkRequestBuilder<NfcCheckInWorker>()
                .setInputData(inputData)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

        workManager.enqueue(workRequest)
    }
}
