package com.mk.habittracker.feature.habitdetail

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mk.habittracker.core.data.HabitRepository
import com.mk.habittracker.core.model.CheckIn
import com.mk.habittracker.core.model.Habit
import com.mk.habittracker.core.model.HabitDetail
import com.mk.habittracker.core.model.HabitStats
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HabitDetailViewModelTest {
    private val repository: HabitRepository = mockk()
    private val auth: FirebaseAuth = mockk()
    private val firebaseUser: FirebaseUser = mockk()
    private val habitId = "test-habit-id"
    private val userId = "test-user-id"

    private val defaultHabit = Habit(habitId, userId, "Drink Water", 123456L)
    private val defaultStats = HabitStats(0, 0, 0)
    private val defaultDetail = HabitDetail(defaultHabit, defaultStats)

    @Before
    fun setup() {
        every { auth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns userId

        // Default returns to avoid crash during init
        every { repository.getHabitDetail(habitId) } returns flowOf(defaultDetail)
        every { repository.getCheckIns(any(), habitId) } returns flowOf(emptyList())
    }

    @Test
    fun `habitDetail reflects repository data`() = runTest {
        val habit = Habit(habitId, userId, "Exercise", 123456L)
        val stats = HabitStats(5, 10, 50)
        val detail = HabitDetail(habit, stats)

        every { repository.getHabitDetail(habitId) } returns flowOf(detail)

        val viewModel = HabitDetailViewModel(habitId, repository, auth)

        viewModel.habitDetail.test {
            assertThat(awaitItem()).isEqualTo(detail)
        }
    }

    @Test
    fun `checkIns reflects repository data`() = runTest {
        val checkInList = listOf(
            CheckIn("1", habitId, userId, LocalDate.now()),
            CheckIn("2", habitId, userId, LocalDate.now().minusDays(1)),
        )

        every { repository.getCheckIns(userId, habitId) } returns flowOf(checkInList)

        val viewModel = HabitDetailViewModel(habitId, repository, auth)

        viewModel.checkIns.test {
            assertThat(awaitItem()).isEqualTo(checkInList)
        }
    }

    @Test
    fun `userId uses anonymous when not logged in`() = runTest {
        every { auth.currentUser } returns null
        every { repository.getCheckIns("anonymous", habitId) } returns flowOf(emptyList())

        HabitDetailViewModel(habitId, repository, auth)

        verify { repository.getCheckIns("anonymous", habitId) }
    }
}
