package mk.habittracker

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.TagTechnology
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.collection.intListOf
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import dagger.hilt.android.AndroidEntryPoint
import mk.habittracker.nfc.NfcReaderModeController
import mk.habittracker.nfc.TagBus
import mk.habittracker.ui.AppNavigation
import mk.habittracker.ui.theme.HabitTrackerTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var nfcReaderModeController: NfcReaderModeController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(nfcReaderModeController)

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