package mk.habittracker.nfc

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.FLAG_READER_NFC_A
import android.nfc.NfcAdapter.FLAG_READER_NFC_B
import android.nfc.NfcAdapter.FLAG_READER_NFC_BARCODE
import android.nfc.NfcAdapter.FLAG_READER_NFC_F
import android.nfc.NfcAdapter.FLAG_READER_NFC_V
import android.nfc.Tag
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import javax.inject.Inject

private const val READER_FLAGS =
    FLAG_READER_NFC_A or
        FLAG_READER_NFC_B or
        FLAG_READER_NFC_F or
        FLAG_READER_NFC_V or
        FLAG_READER_NFC_BARCODE

class NfcReaderModeController
    @Inject
    constructor(
        val activity: Activity,
        val nfcAdapter: NfcAdapter?,
        val tagBus: TagBus,
    ) : DefaultLifecycleObserver,
        NfcAdapter.ReaderCallback {
        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            Log.d("nfc", "[NfcReaderModeController#onStart] enabling reader mode")
            nfcAdapter?.enableReaderMode(activity, this, READER_FLAGS, null)
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            Log.d("nfc", "[NfcReaderModeController#onStop] disabling reader mode")
            nfcAdapter?.disableReaderMode(activity)
        }

        override fun onTagDiscovered(p0: Tag?) {
            Log.d("nfc", "[NfcReaderModeController#onTagDiscovered] sending tag to bus")
            p0?.let { tagBus.add(it) }
        }
    }
