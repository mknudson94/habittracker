package mk.habittracker.nfc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import javax.inject.Inject

@ActivityScoped
class NfcController @Inject constructor(
    @ActivityContext private val context: Context
) {
    private val activity = context as Activity
    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(activity)

    // The currently "interested" screen's callback, if any.
    private var activeCallback: ((List<NdefMessage>) -> Unit)? = null

    fun isAvailable(): Boolean = nfcAdapter != null

    /** Called by a Composable screen (via DisposableEffect) that wants tag events. */
    fun startListening(onTag: (List<NdefMessage>) -> Unit) {
        activeCallback = onTag
        val flags = NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

        nfcAdapter?.enableReaderMode(activity, { tag ->
            val messages = extractMessages(tag)
            if (messages.isNotEmpty()) activeCallback?.invoke(messages)
        }, flags, null)
    }

    /** Called when the screen leaves composition. */
    fun stopListening() {
        nfcAdapter?.disableReaderMode(activity)
        activeCallback = null
    }

    /** Called from the Activity's onNewIntent for AAR/background dispatch. */
    fun handleIntent(intent: Intent?) {
        val messages = extractIntentMessages(intent)
        if (messages.isNotEmpty()) activeCallback?.invoke(messages)
    }

    private fun extractMessages(tag: Tag): List<NdefMessage> {
        val ndef = Ndef.get(tag) ?: return emptyList()
        return try {
            ndef.connect()
            listOfNotNull(ndef.cachedNdefMessage ?: ndef.ndefMessage)
        } catch (e: Exception) {
            emptyList()
        } finally {
            runCatching { ndef.close() }
        }
    }

    private fun extractIntentMessages(intent: Intent?): List<NdefMessage> {
        if (intent?.action != NfcAdapter.ACTION_NDEF_DISCOVERED) return emptyList()
        val messages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.map { it as NdefMessage }?.toTypedArray()
        }
        return messages?.toList() ?: emptyList()
    }
}