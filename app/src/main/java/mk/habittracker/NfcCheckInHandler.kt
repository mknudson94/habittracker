package mk.habittracker

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import mk.habittracker.data.dao.HabitDao
import mk.habittracker.data.model.CheckIn
import mk.habittracker.nfc.TagBus
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Singleton
class NfcCheckInHandler @Inject constructor(
    private val habitDao: HabitDao,
    private val tagBus: TagBus,
) : DefaultLifecycleObserver {

    private var checkInJob: Job? = null

    suspend fun handleIntent(intent: Intent) {
        // Ndef encoded intents provide messages directly
        if (intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED) {
            val messages: Array<NdefMessage>? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, NdefMessage::class.java)?.filterIsInstance<NdefMessage>()?.toTypedArray()
            } else {
                intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)?.filterIsInstance<NdefMessage>()?.toTypedArray()
            }
            Log.d("intent", "launching async handler from messages: $messages")
            messages?.let { handleIntent(it) }
        }
        else {
            val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
            Log.d("intent", "tag: $tag")
            Log.d("intent", "ndef: ${tag?.let { Ndef.get(it) }?.ndefMessage?.records}")
        }
    }

    suspend fun handleIntent(messages: Array<NdefMessage>) {
        // todo: robust checking

        Log.d("intent", "handling nfc intent with messages:")
        Log.d("intent", messages.flatMap { message ->  message.records.map { "$it, " } }.toString())
        check(messages.isNotEmpty(), { "error, empty messages array" })
        val records = messages.first().records
        val habitRecord = records.firstOrNull() ?: error("message has no records")
        val habitId = habitRecord.payload.toString(Charsets.UTF_8)

        Log.d("intent", "attempting to add check-in for habit $habitId")
        val checkIn = CheckIn(
            id = Uuid.random().toString(),
            habitId = habitId,
            completedDate = LocalDate.now(),
        )
        Log.d("intent", "check-in: $checkIn")
        habitDao.addCheckIn(checkIn)
    }

    suspend fun handleReader(tag: Tag) {
        val ndef = Ndef.get(tag) ?: error("null ndef")
        val message = ndef.cachedNdefMessage ?: ndef.ndefMessage ?: error("null message")
        val habitId = message.records.first().payload.toString(Charsets.UTF_8)
        Log.d("NfcCheckInHandler", "handling readerMode for habitId")
        habitDao.addCheckIn(
            CheckIn(
                id = Uuid.random().toString(),
                habitId = habitId,
                completedDate = LocalDate.now(),
            )
        )
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        Log.d("NfcCheckInHandler", "starting checkInJob")
        if (checkInJob?.isActive == true) {
            Log.d("NfcCheckInHandler", "why tf is this job already active")
        }
        checkInJob = tagBus.tags
            .combine(tagBus.subscribers) { tag, subscriberCount -> tag to subscriberCount }
            .onEach { (tag, subscriberCount) ->
                Log.d("NfcCheckInHandler", "received combine event with tag: $tag and subscriberCount: $subscriberCount")
                if (subscriberCount == 1) handleReader(tag)
            }.launchIn(owner.lifecycleScope)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        Log.d("NfcCheckInHandler", "canceling checkInJob")
        checkInJob?.cancel()
    }
}