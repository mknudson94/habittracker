package com.mk.habittracker.feature.pairnfc

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mk.habittracker.core.nfc.NfcReaderModeFlag
import com.mk.habittracker.core.nfc.TagBus
import com.mk.habittracker.core.nfc.WriteNfcResult
import com.mk.habittracker.core.nfc.WriteNfcTagUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach

sealed class PairNfcTagState {
    data object ReadyToScan : PairNfcTagState()

    data class ConfirmOverwrite(
        val confirmed: Boolean = false,
    ) : PairNfcTagState()

    data class Error(
        val message: String,
    ) : PairNfcTagState()

    data class Success(val tagId: ByteArray) : PairNfcTagState() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as Success
            return tagId.contentEquals(other.tagId)
        }

        override fun hashCode(): Int {
            return tagId.contentHashCode()
        }
    }
}

@HiltViewModel(assistedFactory = NfcPairingViewModel.Factory::class)
class NfcPairingViewModel @AssistedInject constructor(
    @Assisted private val habitId: String,
    private val tagBus: TagBus,
    private val writeNfcTagUseCase: WriteNfcTagUseCase,
    private val nfcReaderModeFlag: NfcReaderModeFlag,
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
        nfcReaderModeFlag.requestReaderMode()
        writeNfcTagJob =
            tagBus.tags
                .onEach { tag ->
                    Log.d("nfc", "[NfcPairingViewModel#tags.onEach] received tag from bus")
                    val result =
                        writeNfcTagUseCase.execute(
                            tag = tag,
                            habitId = habitId,
                            shouldOverwrite =
                                (_pairingState.value as? PairNfcTagState.ConfirmOverwrite)?.confirmed == true,
                        )
                    _pairingState.value =
                        when (result) {
                            is WriteNfcResult.Success -> PairNfcTagState.Success(result.tagId)
                            WriteNfcResult.DidNotOverwrite -> PairNfcTagState.ConfirmOverwrite()
                            is WriteNfcResult.Error -> PairNfcTagState.Error(result.message)
                        }
                }
                .onCompletion {
                    Log.d("nfc", "[NfcPairingViewModel#tags.onCompletion] cleaning up; cause = $it")
                    nfcReaderModeFlag.releaseReaderMode()
                }
                .launchIn(viewModelScope)
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
                            nfcReaderModeFlag.requestReaderMode()
                            startWriteNfcTagJob()
                        } else {
                            Log.d("nfc", "[NfcPairingViewModel#init] canceling write job")
                            writeNfcTagJob?.cancel()
                        }
                    }

                    is PairNfcTagState.Error, is PairNfcTagState.Success -> {
                        Log.d("nfc", "[NfcPairingViewModel#init] canceling write job")
                        nfcReaderModeFlag.releaseReaderMode()
                        writeNfcTagJob?.cancel()
                    }
                }
            }.launchIn(viewModelScope)
    }

    override fun onCleared() {
        nfcReaderModeFlag.releaseReaderMode()
    }

    fun confirmOverwrite() {
        require(_pairingState.value is PairNfcTagState.ConfirmOverwrite)
        _pairingState.value =
            (_pairingState.value as PairNfcTagState.ConfirmOverwrite).copy(confirmed = true)
    }

    fun tryAgain() {
        _pairingState.value = PairNfcTagState.ReadyToScan
    }
}
