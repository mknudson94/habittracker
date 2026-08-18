package com.mk.habittracker.ui

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import com.google.firebase.auth.FirebaseUser
import com.mk.habittracker.HiltTestActivity
import com.mk.habittracker.core.ui.theme.HabitTrackerTheme
import com.mk.habittracker.feature.auth.LoginViewModel
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [34])
class AppNavigationTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<HiltTestActivity>()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun app_starts_on_login_when_not_authenticated() {
        val viewModel: LoginViewModel = mockk(relaxed = true)
        every { viewModel.authState } returns MutableStateFlow(null)

        composeTestRule.setContent {
            HabitTrackerTheme {
                AppNavigation(viewModel = viewModel)
            }
        }

        composeTestRule.onAllNodes(hasText("Sign in", substring = false)).onFirst().assertExists()
    }

    @Test
    fun app_starts_on_home_when_authenticated() {
        val viewModel: LoginViewModel = mockk(relaxed = true)
        val mockUser: FirebaseUser = mockk()
        every { mockUser.uid } returns "test-user"
        every { viewModel.authState } returns MutableStateFlow(mockUser)

        composeTestRule.setContent {
            HabitTrackerTheme {
                AppNavigation(viewModel = viewModel)
            }
        }

        composeTestRule.onAllNodes(hasText("Habits", substring = true)).onFirst().assertExists()
    }
}
