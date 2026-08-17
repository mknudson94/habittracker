package com.mk.habittracker.nfccheckin

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mk.habittracker.core.data.HabitRepository
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UndoCheckInReceiverTest {

    private val repository: HabitRepository = mockk(relaxed = true)
    private val auth: FirebaseAuth = mockk()
    private val firebaseUser: FirebaseUser = mockk()
    private lateinit var context: Context
    
    private lateinit var receiver: UndoCheckInReceiver
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        Dispatchers.setMain(testDispatcher)
        receiver = UndoCheckInReceiver()
        receiver.repository = repository
        receiver.auth = auth
        
        every { auth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns "user-1"
    }

    @Test
    fun `handleIntent calls deleteCheckIn with correct parameters`() = runTest {
        val habitId = "habit-1"
        val intent = Intent(ACTION_UNDO_CHECKIN).apply {
            putExtra(EXTRA_HABIT_ID, habitId)
        }

        receiver.handleIntent(context, intent)
        advanceUntilIdle()

        coVerify {
            repository.deleteCheckIn(habitId, LocalDate.now(), "user-1")
        }
    }

    @Test
    fun `handleIntent ignores intents with wrong action`() = runTest {
        val intent = Intent("WRONG_ACTION")

        receiver.handleIntent(context, intent)
        advanceUntilIdle()

        coVerify(exactly = 0) {
            repository.deleteCheckIn(any(), any(), any())
        }
    }
}
