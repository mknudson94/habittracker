package com.mk.habittracker.feature.habitdetail

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
import com.mk.habittracker.core.nfc.WriteNfcResult
import com.mk.habittracker.core.nfc.WriteNfcTagUseCase
import com.mk.habittracker.core.nfc.TagBus

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

        private val _pairingState = MutableStateFlow<PairNfcTagState>(PairNfcTagState.ReadyToScan)
        val pairingState = _pairingState.asStateFlow()

        private var writeNfcTagJob: Job? = null

        private fun startWriteNfcTagJob() {
            if (writeNfcTagJob?.isActive == true) return
            writeNfcTagJob =
                tagBus.tags
                    .onEach { tag ->
                        Log.d("nfc", "[NfcPairingViewModel#tags.onEach] received tag from bus")
                        val result =
                            writeNfcTagUseCase.execute(
                                tag = tag,
                                message = buildMessage(),
                                shouldOverwrite =
                                    (_pairingState.value as? PairNfcTagState.ConfirmOverwrite)?.confirmed == true,
                            )
                        _pairingState.value =
                            when (result) {
                                WriteNfcResult.Success -> PairNfcTagState.Success
                                WriteNfcResult.DidNotOverwrite -> PairNfcTagState.ConfirmOverwrite()
                                is WriteNfcResult.Error -> PairNfcTagState.Error(result.message)
                            }
                    }.launchIn(viewModelScope)
        }

        init {
            Log.d("nfc", "[NfcPairingViewModel#init]")
            _pairingState
                .onEach {
                    when (it) {
                        PairNfcTagState.ReadyToScan -> {
                            Log.d("nfc", "[NfcPairingViewModel#init] starting write job")
                            startWriteNfcTagJob()
                        }
                        is PairNfcTagState.ConfirmOverwrite -> {
                            if (it.confirmed) {
                                Log.d("nfc", "[NfcPairingViewModel#init] starting re-write job")
                                startWriteNfcTagJob()
                            } else {
                                Log.d("nfc", "[NfcPairingViewModel#init] canceling write job")
                                writeNfcTagJob?.cancel()
                            }
                        }
                        is PairNfcTagState.Error, PairNfcTagState.Success -> {
                            Log.d("nfc", "[NfcPairingViewModel#init] canceling write job")
                            writeNfcTagJob?.cancel()
                        }
                    }
                }.launchIn(viewModelScope)
        }

        fun confirmOverwrite() {
            require(_pairingState.value is PairNfcTagState.ConfirmOverwrite)
            _pairingState.value = (_pairingState.value as PairNfcTagState.ConfirmOverwrite).copy(confirmed = true)
        }

        private fun buildMessage(): NdefMessage =
            NdefMessage(
                NdefRecord.createExternal(
                    "com.mk.habittracker",
                    "habit_tag",
                    habitId.toByteArray(),
                ),
                NdefRecord.createApplicationRecord("com.mk.habittracker"),
            )
    }
