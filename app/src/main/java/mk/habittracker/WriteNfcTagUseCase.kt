package mk.habittracker

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import javax.inject.Inject

class WriteNfcTagUseCase @Inject constructor() {
    fun execute(
        tag: Tag,
        message: NdefMessage,
        shouldOverwrite: Boolean
    ): WriteNfcResult {
        val ndef = Ndef.get(tag)
        return try {
             if (ndef == null) error("Tag doesn't support NDEF")
            ndef.connect()

            if (!ndef.isWritable) {
                error("This tag is read-only")
            }

            if (!ndef.isBlank() && !shouldOverwrite) {
                WriteNfcResult.DidNotOverwrite
            } else {
                ndef.writeNdefMessage(message)
                WriteNfcResult.Success
            }
        } catch (e: Exception) {
            WriteNfcResult.Error(e.localizedMessage.orEmpty())
        } finally {
            ndef.close()
        }
    }
}

sealed class WriteNfcResult {
    data object Success : WriteNfcResult()
    data object DidNotOverwrite : WriteNfcResult()
    data class Error(val message: String) : WriteNfcResult()
}

private fun Ndef.isBlank(): Boolean {
    val message = cachedNdefMessage ?: ndefMessage
    if (message == null) return true

    val isWellFormedBlankRecord =
        message.records.size == 1 && message.records.first().tnf == NdefRecord.TNF_EMPTY

    return  isWellFormedBlankRecord
}
