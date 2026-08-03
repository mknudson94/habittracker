package com.mk.habittracker.core.nfc

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.mk.habittracker.core.data.HabitRepository
import com.mk.habittracker.core.model.CheckIn
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Singleton
class NfcCheckInHandler @Inject constructor(
    private val repository: HabitRepository,
    private val tagBus: TagBus,
    private val nfcReaderModeFlag: NfcReaderModeFlag,
) : DefaultLifecycleObserver {
    private var checkInJob: Job? = null

    private val userId: String
        get() = Firebase.auth.currentUser?.uid ?: "anonymous"

    suspend fun handleIntent(intent: Intent) {
        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            val messages: Array<NdefMessage>? =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent
                        .getParcelableArrayExtra(
                            NfcAdapter.EXTRA_NDEF_MESSAGES,
                            NdefMessage::class.java,
                        )?.filterIsInstance<NdefMessage>()
                        ?.toTypedArray()
                } else {
                    @Suppress("DEPRECATION")
                    intent
                        .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
                        ?.filterIsInstance<NdefMessage>()
                        ?.toTypedArray()
                }
            Log.d(
                "intent",
                "launching async handler from messages: ${messages.contentToString()}",
            )
            messages?.let {
                val tagId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)
                handleIntent(
                    nfcUid = tagId ?: byteArrayOf(),
                    messages = it,
                )
            }
        }
    }

    suspend fun handleIntent(
        nfcUid: ByteArray,
        messages: Array<NdefMessage>
    ) {
        Log.d("intent", "handling nfc intent with messages:")
        check(messages.isNotEmpty()) { "error, empty messages array" }
        val records = messages.first().records
        val habitRecord = records.firstOrNull() ?: error("message has no records")
        val habitId = habitRecord.payload.toString(Charsets.UTF_8)

        Log.d("intent", "attempting to add check-in for habit $habitId")
        val checkIn =
            CheckIn(
                id = Uuid.random().toString(),
                habitId = habitId,
                completedDate = LocalDate.now(),
                nfcUid = nfcUid,
                userId = userId,
            )
        Log.d("intent", "check-in: $checkIn")
        repository.addCheckIn(checkIn)
    }

    suspend fun handleReader(tag: Tag) {
        val ndef = Ndef.get(tag) ?: error("null ndef")
        val message = ndef.cachedNdefMessage ?: ndef.ndefMessage ?: error("null message")
        val habitId =
            message.records
                .first()
                .payload
                .toString(Charsets.UTF_8)
        Log.d("NfcCheckInHandler", "handling readerMode for habitId")
        repository.addCheckIn(
            CheckIn(
                id = Uuid.random().toString(),
                habitId = habitId,
                completedDate = LocalDate.now(),
                nfcUid = tag.id,
                userId = userId,
            ),
        )
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d("NfcCheckInHandler", "starting checkInJob")
        checkInJob = tagBus.tags
            .onEach { tag ->
                Log.d(
                    "NfcCheckInHandler",
                    "received tag: $tag\n\treaderMode is currently ${nfcReaderModeFlag.readerModeRequested.value}",
                )
                // only check in if nobody else is scanning for tag events
                if (!nfcReaderModeFlag.readerModeRequested.value) handleReader(tag)
            }.launchIn(owner.lifecycleScope)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d("NfcCheckInHandler", "canceling checkInJob")
        checkInJob?.cancel()
    }
}
