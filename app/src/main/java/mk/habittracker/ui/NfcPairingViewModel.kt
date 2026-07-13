package mk.habittracker.ui

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import mk.habittracker.WriteNfcResult
import mk.habittracker.WriteNfcTagUseCase
import mk.habittracker.nfc.TagBus
import mk.habittracker.ui.PairNfcTagState.ConfirmOverwrite
import mk.habittracker.ui.PairNfcTagState.Error
import mk.habittracker.ui.PairNfcTagState.ReadyToScan
import mk.habittracker.ui.PairNfcTagState.Success
import kotlin.uuid.ExperimentalUuidApi

sealed class PairNfcTagState {
    data object ReadyToScan : PairNfcTagState()

    data class ConfirmOverwrite(
        val confirmed: Boolean = false,
    ) : PairNfcTagState()

    data class Error(
        val message: String,
    ) : PairNfcTagState()

    data object Success : PairNfcTagState()
}

@OptIn(ExperimentalUuidApi::class)
@HiltViewModel(assistedFactory = NfcPairingViewModel.Factory::class)
class NfcPairingViewModel
    @AssistedInject
    constructor(
        @Assisted private val habitId: String,
        private val tagBus: TagBus,
        private val writeNfcTagUseCase: WriteNfcTagUseCase,
    ) : ViewModel() {
        @AssistedFactory
        interface Factory {
            fun create(habitId: String): NfcPairingViewModel
        }

        private val _pairingState = MutableStateFlow<PairNfcTagState>(ReadyToScan)
        val pairingState = _pairingState.asStateFlow()

        private var writeNfcTagJob: Job? = null

        private fun startWriteNfcTagJob() {
            if (writeNfcTagJob?.isActive == true) return
            writeNfcTagJob =
                tagBus.tags
                    .onEach { tag ->
                        Log.d("nfc", "[AddHabitViewModel#tags.onEach] received tag from bus")
                        val result =
                            writeNfcTagUseCase.execute(
                                tag = tag,
                                message = buildMessage(),
                                shouldOverwrite =
                                    (_pairingState.value as? ConfirmOverwrite)?.confirmed == true,
                            )
                        _pairingState.value =
                            when (result) {
                                WriteNfcResult.Success -> Success
                                WriteNfcResult.DidNotOverwrite -> ConfirmOverwrite()
                                is WriteNfcResult.Error -> Error(result.message)
                            }
                    }.launchIn(viewModelScope)
        }

        init {
            Log.d("nfc", "[AddHabitViewModel#init]")
            _pairingState
                .onEach {
                    when (it) {
                        ReadyToScan -> {
                            Log.d("nfc", "[AddHabitViewModel#init] starting write job")
                            startWriteNfcTagJob()
                        }
                        is ConfirmOverwrite -> {
                            if (it.confirmed) {
                                Log.d("nfc", "[AddHabitViewModel#init] starting re-write job")
                                startWriteNfcTagJob()
                            } else {
                                Log.d("nfc", "[AddHabitViewModel#init] canceling write job")
                                writeNfcTagJob?.cancel()
                            }
                        }
                        is Error, Success -> {
                            Log.d("nfc", "[AddHabitViewModel#init] canceling write job")
                            writeNfcTagJob?.cancel()
                        }
                    }
                }.launchIn(viewModelScope)
        }

        fun confirmOverwrite() {
            require(_pairingState.value is ConfirmOverwrite)
            _pairingState.value = (_pairingState.value as ConfirmOverwrite).copy(confirmed = true)
        }

        private fun buildMessage(): NdefMessage =
            NdefMessage(
                NdefRecord.createExternal(
                    "mk.habittracker",
                    "habit_tag",
                    habitId.toByteArray(),
                ),
                NdefRecord.createApplicationRecord("com.example.habittracker"),
            )
    }
