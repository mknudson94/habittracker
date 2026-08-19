package com.mk.habittracker.nfccheckin

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mk.habittracker.core.data.HabitRepository
import com.mk.habittracker.core.model.Habit
import com.mk.habittracker.core.nfc.NfcCheckInHandler
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class NfcCheckInWorkerTest {
    private lateinit var context: Context
    private val nfcCheckInHandler: NfcCheckInHandler = mockk(relaxed = true)
    private val habitRepository: HabitRepository = mockk(relaxed = true)
    private val auth: FirebaseAuth = mockk()
    private val firebaseUser: FirebaseUser = mockk()

    private val habitId = "habit-1"
    private val userId = "user-1"
    private val tagId = byteArrayOf(1, 2, 3)

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        every { auth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns userId

        val habit = Habit(habitId, userId, "Drink Water", 123456L)
        every { habitRepository.getHabit(userId, habitId) } returns flowOf(habit)
        coEvery { habitRepository.hasCheckedInToday(habitId) } returns false
        coEvery { habitRepository.getCurrentStreak(userId, habitId) } returns 5
    }

    @Test
    fun `doWork performs checkin when not already checked in`() = runBlocking {
        val worker = TestListenableWorkerBuilder<NfcCheckInWorker>(
            context = context,
            inputData = workDataOf(
                NFC_UID_KEY to tagId,
                NFC_HABIT_ID_KEY to habitId,
            ),
        ).setWorkerFactory(
            object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters,
                ) = NfcCheckInWorker(
                    appContext,
                    workerParameters,
                    nfcCheckInHandler,
                    habitRepository,
                    auth,
                )
            },
        ).build()

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify {
            nfcCheckInHandler.checkIn(
                match {
                    it.habitId == habitId && it.uid.contentEquals(tagId)
                },
            )
        }
    }

    @Test
    fun `doWork does not perform checkin when already checked in`() = runBlocking {
        coEvery { habitRepository.hasCheckedInToday(habitId) } returns true

        val worker = TestListenableWorkerBuilder<NfcCheckInWorker>(
            context = context,
            inputData = workDataOf(
                NFC_HABIT_ID_KEY to habitId,
            ),
        ).setWorkerFactory(
            object : androidx.work.WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: androidx.work.WorkerParameters,
                ): ListenableWorker = NfcCheckInWorker(
                    appContext,
                    workerParameters,
                    nfcCheckInHandler,
                    habitRepository,
                    auth,
                )
            },
        ).build()

        val result = worker.doWork()

        assertThat(result).isEqualTo(ListenableWorker.Result.success())
        coVerify(exactly = 0) {
            nfcCheckInHandler.checkIn(any())
        }
    }
}
