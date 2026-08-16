package com.mk.habittracker.core.nfc

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.util.Log

/**
 * specifically for NDEF tags formatted by [PairNfcViewModel#createMessage]
 * uid might be null/empty
 */
fun Intent.parseHabitTrackerNdef(): HabitTrackerNdef? {
    if (action != NfcAdapter.ACTION_NDEF_DISCOVERED) {
        Log.w("Nfc", "intent is not of type ACTION_NDEF_DISCOVERED")
        return null
    }
    val tagId = getByteArrayExtra(NfcAdapter.EXTRA_ID)
    val messages: Array<NdefMessage>? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES,
                NdefMessage::class.java,
            )?.filterIsInstance<NdefMessage>()
                ?.toTypedArray()
        } else {
            @Suppress("DEPRECATION")
            getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                ?.filterIsInstance<NdefMessage>()
                ?.toTypedArray()
        }
    check(
        !messages.isNullOrEmpty() &&
            messages.size == 1 &&
            messages.first().records.size == 2,
    ) { "error, bad ndef" }
    val records = messages.first().records
    val habitRecord = records.first()
    val habitId = habitRecord.payload.toString(Charsets.UTF_8)
    return HabitTrackerNdef(
        uid = tagId ?: byteArrayOf(),
        habitId = habitId,
    )
}

fun Tag.parseHabitTrackerNdef(): HabitTrackerNdef {
    val ndef = Ndef.get(this) ?: error("null ndef")
    val message = ndef.cachedNdefMessage ?: ndef.ndefMessage ?: error("null message")
    val habitId =
        message.records
            .first()
            .payload
            .toString(Charsets.UTF_8)
    return HabitTrackerNdef(
        uid = this.id,
        habitId = habitId,
    )
}

data class HabitTrackerNdef(
    val uid: ByteArray,
    val habitId: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HabitTrackerNdef

        if (!uid.contentEquals(other.uid)) return false
        if (habitId != other.habitId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uid.contentHashCode()
        result = 31 * result + habitId.hashCode()
        return result
    }
}
