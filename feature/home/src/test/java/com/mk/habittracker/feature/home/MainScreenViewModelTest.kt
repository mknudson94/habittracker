package com.mk.habittracker.feature.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.mk.habittracker.core.data.HabitRepository
import com.mk.habittracker.core.data.OnboardingPrefs
import com.mk.habittracker.core.model.CheckIn
import com.mk.habittracker.core.model.Habit
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalUuidApi::class)
@RunWith(RobolectricTestRunner::class)
class MainScreenViewModelTest {
    private val repository: HabitRepository = mockk(relaxed = true)
    private val auth: FirebaseAuth = mockk()
    private val onboardingPrefs: OnboardingPrefs = mockk(relaxed = true)
    private val firebaseUser: FirebaseUser = mockk()
    private val testDispatcher = UnconfinedTestDispatcher()

    private val userId = "test-user-id"
    private val habitsList = listOf(
        Habit("1", userId, "Habit 1", 100L),
        Habit("2", userId, "Habit 2", 200L),
    )

    private lateinit var viewModel: MainScreenViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        every { auth.currentUser } returns firebaseUser
        every { firebaseUser.uid } returns userId
        every { repository.getHabits(userId) } returns flowOf(habitsList)
        every { auth.addAuthStateListener(any()) } just runs

        viewModel = MainScreenViewModel(repository, auth, onboardingPrefs)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `habits reflects repository data`() = runTest {
        viewModel.habits.test {
            assertThat(awaitItem()).isEqualTo(habitsList)
        }
    }

    @Test
    fun `getCheckIns returns flow from repository`() = runTest {
        val habitId = "1"
        val checkIns = listOf(CheckIn("c1", habitId, userId, LocalDate.now()))
        every { repository.getCheckIns(userId, habitId) } returns flowOf(checkIns)

        viewModel.getCheckIns(habitId).test {
            assertThat(awaitItem()).isEqualTo(checkIns)
        }
    }

    @Test
    fun `toggleCheckIn true calls addCheckIn`() = runTest {
        val habitId = "1"
        viewModel.toggleCheckIn(true, habitId)

        coVerify {
            repository.addCheckIn(
                match {
                    it.habitId == habitId &&
                        it.userId == userId &&
                        it.completedDate == LocalDate.now()
                },
            )
        }
    }

    @Test
    fun `toggleCheckIn false calls deleteCheckIn`() = runTest {
        val habitId = "1"
        viewModel.toggleCheckIn(false, habitId)

        coVerify {
            repository.deleteCheckIn(habitId, LocalDate.now(), userId)
        }
    }

    @Test
    fun `anonymous userId is used when not logged in`() = runTest {
        every { auth.currentUser } returns null
        every { repository.getHabits("anonymous") } returns flowOf(emptyList())

        // Re-init to use the new auth state
        val vm = MainScreenViewModel(repository, auth, onboardingPrefs)

        vm.habits.test {
            assertThat(awaitItem()).isEmpty()
        }

        vm.toggleCheckIn(true, "habit1")

        coVerify {
            repository.addCheckIn(match { it.userId == "anonymous" })
        }
    }

    @Test
    fun `shouldShowTooltip is true when no habits and onboarding not done`() = runTest {
        every { repository.getHabits(userId) } returns flowOf(emptyList())
        every { onboardingPrefs.hasCreatedFirstHabit } returns flowOf(false)

        val vm = MainScreenViewModel(repository, auth, onboardingPrefs)

        vm.shouldShowTooltip.test {
            assertThat(awaitItem()).isTrue()
        }
    }

    @Test
    fun `shouldShowTooltip is false when habits exist`() = runTest {
        every { repository.getHabits(userId) } returns flowOf(habitsList)
        every { onboardingPrefs.hasCreatedFirstHabit } returns flowOf(false)

        val vm = MainScreenViewModel(repository, auth, onboardingPrefs)

        vm.shouldShowTooltip.test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `shouldShowTooltip is false when onboarding done`() = runTest {
        every { repository.getHabits(userId) } returns flowOf(emptyList())
        every { onboardingPrefs.hasCreatedFirstHabit } returns flowOf(true)

        val vm = MainScreenViewModel(repository, auth, onboardingPrefs)

        vm.shouldShowTooltip.test {
            assertThat(awaitItem()).isFalse()
        }
    }

    @Test
    fun `habits flow updates when auth state changes`() = runTest {
        val listenerSlot = slot<FirebaseAuth.AuthStateListener>()
        every { auth.addAuthStateListener(capture(listenerSlot)) } just runs

        val vm = MainScreenViewModel(repository, auth, onboardingPrefs)

        vm.habits.test {
            assertThat(awaitItem()).isEqualTo(habitsList) // Initial

            // Simulate logout
            every { auth.currentUser } returns null
            every { repository.getHabits("anonymous") } returns flowOf(emptyList())

            listenerSlot.captured.onAuthStateChanged(auth)

            assertThat(awaitItem()).isEmpty()
        }
    }
}
