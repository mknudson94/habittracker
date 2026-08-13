package com.mk.habittracker.core.nfc

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import javax.inject.Inject

class WriteNfcTagUseCase @Inject constructor() {
    fun execute(
        tag: Tag,
        habitId: String,
        shouldOverwrite: Boolean,
    ): WriteNfcResult {
        val ndef = Ndef.get(tag)
        return try {
            if (ndef == null) error("Tag doesn't support NDEF")
            ndef.connect()

            if (!ndef.isWritable) error("This tag is read-only")

            if (!ndef.isBlank() && !shouldOverwrite) {
                WriteNfcResult.DidNotOverwrite
            } else {
                ndef.writeNdefMessage(buildMessage(habitId))
                WriteNfcResult.Success(tag.id)
            }
        } catch (e: Exception) {
            WriteNfcResult.Error(e.localizedMessage.orEmpty())
        } finally {
            if (ndef != null) ndef.close()
        }
    }
}

sealed class WriteNfcResult {
    data class Success(val tagId: ByteArray) : WriteNfcResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Success
            return tagId.contentEquals(other.tagId)
        }

        override fun hashCode(): Int {
            return tagId.contentHashCode()
        }
    }

    data object DidNotOverwrite : WriteNfcResult()

    data class Error(
        val message: String,
    ) : WriteNfcResult()
}

private fun Ndef.isBlank(): Boolean {
    val message = cachedNdefMessage ?: ndefMessage
    if (message == null) return true

    val isWellFormedBlankRecord =
        message.records.size == 1 && message.records.first().tnf == NdefRecord.TNF_EMPTY

    return isWellFormedBlankRecord
}

private fun buildMessage(habitId: String): NdefMessage =
    NdefMessage(
        NdefRecord.createExternal(
            "com.mk.habittracker",
            "habit_tag",
            habitId.toByteArray(),
        ),
        NdefRecord.createApplicationRecord("com.mk.habittracker"),
    )
