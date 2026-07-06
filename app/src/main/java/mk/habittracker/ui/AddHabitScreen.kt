package mk.habittracker.ui

import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.tech.Ndef
import android.util.Log
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel

private const val TAG = "nfc"
private val RTD_ANDROID_APP = "android.com:pkg".toByteArray()

@Composable
fun AddHabitScreen(
    vm: AddHabitViewModel = hiltViewModel()
) {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitScreen(
    onDismiss: () -> Unit
) {
    val nfcBottomSheetState = rememberModalBottomSheetState()
    // TODO: use scope to close sheet state - https://developer.android.com/develop/ui/compose/components/bottom-sheets#control-sheet-state
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    FullSheet(
        title = "Add a habit",
        onBack = onDismiss,
    ) {
        Column {
            Text("Add habit", style = MaterialTheme.typography.displayMedium)
        }
        // TODO: hide this when device not NFC capable
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { showBottomSheet = true}
        ) {
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = "",
            )
            Text("Link NFC tag (optional)")
        }
    }
    if (showBottomSheet) {
        NfcBottomSheet(
            state = nfcBottomSheetState,
            onDismiss = { showBottomSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullSheet(
    title: String,
    onBack: () -> Unit,
    content: @Composable BoxScope.() -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "back",
                        )
                    }
                },
                expandedHeight = TopAppBarDefaults.TopAppBarExpandedHeight
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            content()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NfcBottomSheet(
    state: SheetState,
    onDismiss: () -> Unit,
) {

    val activity = LocalActivity.current
    DisposableEffect(Unit) {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)
        Log.d(TAG, "enabling reader mode")
        nfcAdapter.enableReaderMode(
            activity,
            { tag ->
                Log.d(TAG, "[reader-mode] Tag discovered: $tag")
                val ndef = Ndef.get(tag)
                if (ndef == null) {
                    Log.d(TAG, "[reader-mode] Tag is not NDEF formatted")
                }
                try {
                    ndef.connect()
                    val message = ndef.cachedNdefMessage ?: ndef.ndefMessage
                    message?.records?.forEach { record ->
                        Log.d(TAG, "[reader-mode]: decoding record")
                        when (record.tnf) {
                            NdefRecord.TNF_WELL_KNOWN -> {
                                when {
                                    record.type.contentEquals(NdefRecord.RTD_TEXT) -> Log.d(TAG, parseTextRecord(record))
                                    record.type.contentEquals(NdefRecord.RTD_URI) -> Log.d(TAG, "RTD_URI")
                                    record.type.contentEquals(NdefRecord.RTD_SMART_POSTER) -> Log.d(TAG, "RTD_SMART_POSTER")
                                    record.type.contentEquals(NdefRecord.RTD_ALTERNATIVE_CARRIER) -> Log.d(TAG, "RTD_ALTERNATIVE_CARRIER")
                                    record.type.contentEquals(NdefRecord.RTD_HANDOVER_CARRIER) -> Log.d(TAG, "RTD_HANDOVER_CARRIER")
                                    record.type.contentEquals(NdefRecord.RTD_HANDOVER_REQUEST) -> Log.d(TAG, "RTD_HANDOVER_REQUEST")
                                    record.type.contentEquals(NdefRecord.RTD_HANDOVER_SELECT) -> Log.d(TAG, "RTD_HANDOVER_SELECT")
                                    else -> Log.d(TAG, "unknown type on tnf_well_known")
                                }
                            }
                            NdefRecord.TNF_EXTERNAL_TYPE -> {
                                when {
                                    record.type.contentEquals(RTD_ANDROID_APP) -> Log.d(TAG, "RTD_ANDROID_APP")
                                    else -> Log.d(TAG, "unknown type on tnf_external_type")
                                }
                            }
                            NdefRecord.TNF_EMPTY -> Log.d(TAG, "TNF_EMPTY")
                            NdefRecord.TNF_MIME_MEDIA -> Log.d(TAG, "TNF_MIME_MEDIA")
                            NdefRecord.TNF_ABSOLUTE_URI -> Log.d(TAG, "TNF_ABSOLUTE_URI")
                            NdefRecord.TNF_UNCHANGED -> Log.d(TAG, "TNF_UNCHANGED")
                            NdefRecord.TNF_UNKNOWN -> Log.d(TAG, "TNF_UNKNOWN")
                            else -> Log.d(TAG, "else")

                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[reader-mode] Failed reading tag", e)
                } finally {
                    try { ndef.close() } catch (_: Exception) {}
                }
            },
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V or NfcAdapter.FLAG_READER_NFC_BARCODE,
            null,
        )


        onDispose {
            Log.d(TAG, "disabling reader mode")
            nfcAdapter.disableReaderMode(activity)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = state,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info, contentDescription = ""
            )
            Text("Ready to scan", style = MaterialTheme.typography.displaySmall)
            Text("Hold your phone near the NFC tag")
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    }
}


private fun parseTextRecord(record: NdefRecord): String {
    val payload = record.payload
    // Byte 0: status byte. Bit 7 = encoding (0=UTF-8,1=UTF-16),
    // bits 0-5 = length of the IANA language code that follows.
    val isUtf16 = (payload[0].toInt() and 0x80) != 0
    val languageCodeLength = payload[0].toInt() and 0x3F
    val charset = if (isUtf16) Charsets.UTF_16 else Charsets.UTF_8

    return String(
        payload,
        1 + languageCodeLength,
        payload.size - 1 - languageCodeLength,
        charset
    )
}