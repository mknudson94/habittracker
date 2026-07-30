package com.mk.habittracker.core.nfc

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
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private const val READER_FLAGS =
    FLAG_READER_NFC_A or
        FLAG_READER_NFC_B or
        FLAG_READER_NFC_F or
        FLAG_READER_NFC_V or
        FLAG_READER_NFC_BARCODE

@ActivityScoped
class NfcReaderModeController @Inject constructor(
    val activity: Activity,
    val nfcReaderModeFlag: NfcReaderModeFlag,
    val nfcAdapter: NfcAdapter?,
    val tagBus: TagBus,
) : DefaultLifecycleObserver, NfcAdapter.ReaderCallback {

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        owner.lifecycleScope.launch {
            nfcReaderModeFlag.readerModeRequested.collectLatest { isRequested ->
                Log.d("NfcReaderModeController", "reader mode requested -> $isRequested")
                if (isRequested) {
                    nfcAdapter?.disableForegroundDispatch(activity)
                    nfcAdapter?.enableReaderMode(
                        /* activity = */ activity,
                        /* callback = */ this@NfcReaderModeController,
                        /* flags = */ READER_FLAGS,
                        /* extras = */ null,
                    )
                } else {
                    nfcAdapter?.disableReaderMode(activity)
                }
            }
        }
        Log.d("nfc", "[NfcReaderModeController#onStart] enabling reader mode")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d("nfc", "[NfcReaderModeController#onStop] disabling reader mode")
        nfcAdapter?.disableReaderMode(activity)
    }

    override fun onTagDiscovered(tag: Tag?) {
        Log.d("nfc", "[NfcReaderModeController#onTagDiscovered] sending tag to bus")
        tag?.let { tagBus.add(it) }
    }
}

@Singleton
class NfcReaderModeFlag @Inject constructor() {
    private val _readerModeRequested = MutableStateFlow(false)
    val readerModeRequested = _readerModeRequested.asStateFlow()

    fun requestReaderMode() {
        _readerModeRequested.value = true
    }

    fun releaseReaderMode() {
        _readerModeRequested.value = false
    }
}
