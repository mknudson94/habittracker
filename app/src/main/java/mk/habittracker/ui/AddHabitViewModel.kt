package mk.habittracker.ui

import android.app.Activity
import android.nfc.FormatException
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter.*
import android.nfc.Tag
import android.nfc.TagLostException
import android.nfc.tech.Ndef
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import mk.habittracker.data.model.Habit
import mk.habittracker.ui.PairNfcTagState.ConfirmOverwrite
import mk.habittracker.ui.PairNfcTagState.Error
import mk.habittracker.ui.PairNfcTagState.Idle
import mk.habittracker.ui.PairNfcTagState.ReadyToScan
import mk.habittracker.ui.PairNfcTagState.Success
import java.io.IOException
import java.time.Instant
import javax.inject.Inject
import kotlin.random.Random

sealed class PairNfcTagState {
    data object Idle : PairNfcTagState()

    data object ReadyToScan : PairNfcTagState()

    data class ConfirmOverwrite(val confirmed: Boolean = false) : PairNfcTagState()

    data class Error(val message: String) : PairNfcTagState()

    data object Success : PairNfcTagState()
}

private const val flags = FLAG_READER_NFC_A or FLAG_READER_NFC_B or FLAG_READER_NFC_F or FLAG_READER_NFC_V or FLAG_READER_NFC_BARCODE

@HiltViewModel
class AddHabitViewModel @Inject constructor(
) : ViewModel() {
    private val _pairingState = MutableStateFlow<PairNfcTagState>(ReadyToScan)
    val pairingState = _pairingState.asStateFlow()

    val pendingHabit = Habit(
        id = Random.nextInt(),
        userId = 1,
        name = "",
        createdAt = Instant.now().toEpochMilli()
    )

    // todo: refactor away from activity in VM
    fun prepareToPair(activity: Activity) {
        val nfcAdapter = getDefaultAdapter(activity)
        nfcAdapter.enableReaderMode(activity, initialPairingCallback, flags, null)
    }

    fun confirmOverwrite() {
        check(_pairingState.value is ConfirmOverwrite)
        _pairingState.value = (_pairingState.value as ConfirmOverwrite).copy(confirmed = true)
    }

    fun prepareToOverwrite(activity: Activity) {
        val nfcAdapter = getDefaultAdapter(activity)
        nfcAdapter.enableReaderMode(activity, overwriteCallback, flags, null)
    }

    fun disableReaderMode(activity: Activity) {
        val nfcAdapter = getDefaultAdapter(activity)
        nfcAdapter.disableReaderMode(activity)
    }


    private val initialPairingCallback = ReaderCallback { tag ->
        val ndef = Ndef.get(tag)
        if (ndef == null) {
            _pairingState.value = Error("Tag is not NDEF formatted")
        }
        try {
            ndef.connect()

            if (!ndef.isWritable) {
                _pairingState.value = Error("This tag is read-only")
            }

            when {
                ndef.isBlank() -> {
                    ndef.writeNdefMessage(buildMessage())
                    _pairingState.value = Success
                }

                else -> {
                    _pairingState.value = ConfirmOverwrite()
                }
            }
        } catch (e: IOException) {
            // if there is an I/O failure, or connect is canceled
            Log.e("nfc", e.toString())
        } catch (e: TagLostException) {
            // if the tag leaves the field
            Log.e("nfc", e.toString())
        } catch (e: SecurityException) {
            // if the tag object is reused after the tag has left the field
            Log.e("nfc", e.toString())
        } catch (e: FormatException) {
            // if the NDEF Message to write is malformed
            Log.e("nfc", e.toString())
        }
    }

    private val overwriteCallback = ReaderCallback { tag ->
        val ndef = Ndef.get(tag)
        try {
            ndef.connect()
            ndef.writeNdefMessage(buildMessage())
            _pairingState.value = Success
        } catch (e: Exception) { // TODO: catch for real
            Log.e("nfc", e.toString())
        }
    }

    private fun buildMessage(): NdefMessage = NdefMessage(
        NdefRecord.createExternal(
            "mk.habittracker",
            "habit_tag",
            pendingHabit.id.toString().toByteArray()
        ),
        NdefRecord.createApplicationRecord("com.example.habittracker")
    )

}


private fun Ndef.isBlank(): Boolean {
    val message = cachedNdefMessage ?: ndefMessage
    if (message == null) return true

    val isWellFormedBlankRecord =
        message.records.size == 1 && message.records.first().tnf == NdefRecord.TNF_EMPTY

    return  isWellFormedBlankRecord
}
