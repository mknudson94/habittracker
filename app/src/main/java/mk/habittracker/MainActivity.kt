package mk.habittracker

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.collection.intListOf
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import mk.habittracker.nfc.NfcReaderModeController
import mk.habittracker.ui.AppNavigation
import mk.habittracker.ui.theme.HabitTrackerTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var nfcReaderModeController: NfcReaderModeController
    @Inject lateinit var nfcCheckInHandler: NfcCheckInHandler

    override fun onNewIntent(intent: Intent) {
        Log.d("intent", "handling intent: ${intent.asString()}")
        super.onNewIntent(intent)
        setIntent(intent)
        lifecycleScope.launch {
            nfcCheckInHandler.handleIntent(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            nfcCheckInHandler.handleIntent(this@MainActivity.intent)
        }

        Log.d("onCreate", intent.asString())

        lifecycle.addObserver(nfcReaderModeController)
        lifecycle.addObserver(nfcCheckInHandler)

        enableEdgeToEdge()
        setContent {
            HabitTrackerTheme {
                AppNavigation()
            }
        }
    }
}

fun createMockTag(message: NdefMessage? = null): Tag {

    val myNdefMessage = message ?: NdefMessage(
        NdefRecord.createExternal(
            "mk.habittracker",
            "habit_tag",
            "1".toByteArray()
        ),
        NdefRecord.createApplicationRecord("com.example.habittracker")
    )

    val ndefBundle = Bundle().apply {
        putInt("maxLength", 48)
        putInt("cardState", 2)   // read/write
        putInt("type", 2)        // Type 2 tag
        putParcelable("ndefmsg", myNdefMessage)
    }

    val nfcABundle = Bundle().apply {
        putByteArray("atqa", byteArrayOf(0x44, 0x00))
        putShort("sak", 0x00)
    }

    // set serviceHandle to 0 and tagService to null to indicate mock tag
    return Tag::class.java.getMethod("createMockTag")
        .invoke(
            /* obj */null, // null for static member
            /* id */ byteArrayOf(0x3F, 0x12, 0x34, 0x56, 0x78, 0x90.toByte(), 0xAB.toByte()),
            /* techList */ intListOf(1, 2, 6, 7),
            /* techExtras */ arrayOf(nfcABundle, ndefBundle),
            /* cookie */ null,
            byteArrayOf(0)) as Tag
}

fun Intent?.asString(): String {
    if (this == null) return ""
    val sb = StringBuilder()
    sb.append("action: ").append(action)
        .append(" data: ").append(dataString)
        .append(" extras: ")
    val extras = getExtras()
    if (extras != null) {
        for (key in extras.keySet()) {
            sb.append(key).append("=").append(extras.get(key)).append(" ")
        }
    }
    return sb.toString()
}