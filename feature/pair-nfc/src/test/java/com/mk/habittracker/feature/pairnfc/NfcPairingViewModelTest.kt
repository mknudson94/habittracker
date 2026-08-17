package com.mk.habittracker.feature.pairnfc

import android.nfc.Tag
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.mk.habittracker.core.nfc.NfcReaderModeFlag
import com.mk.habittracker.core.nfc.TagBus
import com.mk.habittracker.core.nfc.WriteNfcResult
import com.mk.habittracker.core.nfc.WriteNfcTagUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NfcPairingViewModelTest {

    private val tagBus: TagBus = mockk(relaxed = true)
    private val writeNfcTagUseCase: WriteNfcTagUseCase = mockk()
    private val nfcReaderModeFlag: NfcReaderModeFlag = mockk(relaxed = true)
    private val habitId = "test-habit-id"
    private val tagsFlow = MutableSharedFlow<Tag>()
    
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var viewModel: NfcPairingViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { tagBus.tags } returns tagsFlow
        
        viewModel = NfcPairingViewModel(habitId, tagBus, writeNfcTagUseCase, nfcReaderModeFlag)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is ReadyToScan`() = runTest {
        assertThat(viewModel.pairingState.value).isEqualTo(PairNfcTagState.ReadyToScan)
        verify { nfcReaderModeFlag.requestReaderMode() }
    }

    @Test
    fun `successful write transitions to Success state`() = runTest {
        val tag: Tag = mockk()
        val tagId = byteArrayOf(1, 2, 3)
        every { writeNfcTagUseCase.execute(tag, habitId, false) } returns WriteNfcResult.Success(tagId)

        viewModel.pairingState.test {
            assertThat(awaitItem()).isEqualTo(PairNfcTagState.ReadyToScan)
            
            tagsFlow.emit(tag)
            
            assertThat(awaitItem()).isEqualTo(PairNfcTagState.Success(tagId))
        }
        
        verify { nfcReaderModeFlag.releaseReaderMode() }
    }

    @Test
    fun `tag not blank transitions to ConfirmOverwrite`() = runTest {
        val tag: Tag = mockk()
        every { writeNfcTagUseCase.execute(tag, habitId, false) } returns WriteNfcResult.DidNotOverwrite

        viewModel.pairingState.test {
            assertThat(awaitItem()).isEqualTo(PairNfcTagState.ReadyToScan)
            
            tagsFlow.emit(tag)
            
            assertThat(awaitItem()).isEqualTo(PairNfcTagState.ConfirmOverwrite(false))
        }
    }

    @Test
    fun `confirmOverwrite transitions state and requests reader mode`() = runTest {
        val tag: Tag = mockk()
        every { writeNfcTagUseCase.execute(tag, habitId, false) } returns WriteNfcResult.DidNotOverwrite
        
        viewModel.pairingState.test {
            assertThat(awaitItem()).isEqualTo(PairNfcTagState.ReadyToScan)
            tagsFlow.emit(tag)
            assertThat(awaitItem()).isEqualTo(PairNfcTagState.ConfirmOverwrite(false))
            
            viewModel.confirmOverwrite()
            
            assertThat(awaitItem()).isEqualTo(PairNfcTagState.ConfirmOverwrite(true))
        }
        
        verify { nfcReaderModeFlag.requestReaderMode() }
    }

    @Test
    fun `write error transitions to Error state`() = runTest {
        val tag: Tag = mockk()
        val errorMessage = "Write failed"
        every { writeNfcTagUseCase.execute(tag, habitId, false) } returns WriteNfcResult.Error(errorMessage)

        viewModel.pairingState.test {
            assertThat(awaitItem()).isEqualTo(PairNfcTagState.ReadyToScan)
            
            tagsFlow.emit(tag)
            
            assertThat(awaitItem()).isEqualTo(PairNfcTagState.Error(errorMessage))
        }
        
        verify { nfcReaderModeFlag.releaseReaderMode() }
    }

    @Test
    fun `tryAgain resets to ReadyToScan`() = runTest {
        val tag: Tag = mockk()
        every { writeNfcTagUseCase.execute(tag, habitId, false) } returns WriteNfcResult.Error("fail")
        
        viewModel.pairingState.test {
            assertThat(awaitItem()).isEqualTo(PairNfcTagState.ReadyToScan)
            tagsFlow.emit(tag)
            assertThat(awaitItem()).isInstanceOf(PairNfcTagState.Error::class.java)
            
            viewModel.tryAgain()
            
            assertThat(awaitItem()).isEqualTo(PairNfcTagState.ReadyToScan)
        }
    }
}
