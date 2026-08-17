package com.mk.habittracker.nfccheckin

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NfcCheckInTrampolineActivityTest {

    @Before
    fun setup() {
        WorkManagerTestInitHelper.initializeTestWorkManager(RuntimeEnvironment.getApplication())
    }

    @Test
    fun `activity finishes immediately after starting work`() {
        val habitId = "test-habit"
        val ndefRecord1 = NdefRecord.createExternal(
            "com.mk.habittracker",
            "habit",
            habitId.toByteArray()
        )
        val ndefRecord2 = NdefRecord.createApplicationRecord("com.mk.habittracker")
        val ndefMessage = NdefMessage(arrayOf(ndefRecord1, ndefRecord2))
        
        val intent = Intent(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
            putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf(ndefMessage))
            putExtra(NfcAdapter.EXTRA_ID, byteArrayOf(1, 2, 3))
        }

        val controller = Robolectric.buildActivity(NfcCheckInTrampolineActivity::class.java, intent)
        controller.create()

        val activity = controller.get()
        assertThat(activity.isFinishing).isTrue()
    }
}
