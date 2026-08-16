package com.mk.habittracker.core.nfc

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NfcUtilsTest {
    @Test
    fun `parseHabitTrackerNdef parses intent correctly`() {
        val habitId = "habit_123"
        val tagId = byteArrayOf(1, 2, 3, 4)

        val record1 =
            NdefRecord.createExternal(
                "com.mk.habittracker",
                "habit",
                habitId.toByteArray(),
            )
        val record2 =
            NdefRecord.createExternal(
                "com.mk.habittracker",
                "unused",
                "something".toByteArray(),
            )
        val ndefMessage = NdefMessage(arrayOf(record1, record2))

        val intent =
            Intent(NfcAdapter.ACTION_NDEF_DISCOVERED).apply {
                putExtra(NfcAdapter.EXTRA_ID, tagId)
                putExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, arrayOf(ndefMessage))
            }

        val result = intent.parseHabitTrackerNdef()

        assertThat(result).isNotNull()
        assertThat(result?.habitId).isEqualTo(habitId)
        assertThat(result?.uid).isEqualTo(tagId)
    }

    @Test
    fun `parseHabitTrackerNdef returns null for wrong action`() {
        val intent = Intent(Intent.ACTION_VIEW)
        val result = intent.parseHabitTrackerNdef()
        assertThat(result).isNull()
    }
}
