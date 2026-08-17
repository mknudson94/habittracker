package com.mk.habittracker

import android.Manifest
import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.collection.intListOf
import androidx.lifecycle.lifecycleScope
import com.jakewharton.threetenabp.AndroidThreeTen
import com.mk.habittracker.core.nfc.NfcCheckInHandler
import com.mk.habittracker.core.nfc.NfcReaderModeController
import com.mk.habittracker.core.nfc.parseHabitTrackerNdef
import com.mk.habittracker.core.ui.theme.HabitTrackerTheme
import com.mk.habittracker.ui.AppNavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var nfcReaderModeController: NfcReaderModeController

    @Inject lateinit var nfcCheckInHandler: NfcCheckInHandler

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { isGranted: Boolean ->
            if (isGranted) {
                Log.d("MainActivity", "Notification permission granted")
            } else {
                Log.w("MainActivity", "Notification permission denied")
            }
        }

    override fun onNewIntent(intent: Intent) {
        Log.d("intent", "handling intent: ${intent.asString()}")
        super.onNewIntent(intent)
        setIntent(intent)
        lifecycleScope.launch {
            intent.parseHabitTrackerNdef()?.let { nfcCheckInHandler.checkIn(it) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)

        Log.d("onCreate", intent.asString())

        lifecycle.addObserver(nfcReaderModeController)
        lifecycle.addObserver(nfcCheckInHandler)

        enableEdgeToEdge()
        setContent {
            HabitTrackerTheme(dynamicColor = false) {
                AppNavigation()
            }
        }

        // does order matter here?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Suppress("MagicNumber")
fun createMockTag(message: NdefMessage? = null): Tag {
    val myNdefMessage =
        message ?: NdefMessage(
            NdefRecord.createExternal(
                "com.mk.habittracker",
                "habit_tag",
                "1".toByteArray(),
            ),
            NdefRecord.createApplicationRecord("com.mk.habittracker"),
        )

    val ndefBundle =
        Bundle().apply {
            putInt("maxLength", 48)
            putInt("cardState", 2) // read/write
            putInt("type", 2) // Type 2 tag
            putParcelable("ndefmsg", myNdefMessage)
        }

    val nfcABundle =
        Bundle().apply {
            putByteArray("atqa", byteArrayOf(0x44, 0x00))
            putShort("sak", 0x00)
        }

    // set serviceHandle to 0 and tagService to null to indicate mock tag
    return Tag::class.java
        .getMethod("createMockTag")
        .invoke(
            null,
            byteArrayOf(0x3F, 0x12, 0x34, 0x56, 0x78, 0x90.toByte(), 0xAB.toByte()),
            intListOf(1, 2, 6, 7),
            arrayOf(nfcABundle, ndefBundle),
            null,
            byteArrayOf(0),
        ) as Tag
}

fun Intent?.asString(): String {
    if (this == null) return ""
    val sb = StringBuilder()
    sb
        .append("action: ")
        .append(action)
        .append(" data: ")
        .append(dataString)
        .append(" extras: ")
    val extras = getExtras()
    if (extras != null) {
        for (key in extras.keySet()) {
            sb
                .append(key)
                .append("=")
                .append(extras.get(key))
                .append(" ")
        }
    }
    return sb.toString()
}
