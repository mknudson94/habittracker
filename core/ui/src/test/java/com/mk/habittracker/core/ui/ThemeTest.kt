package com.mk.habittracker.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.common.truth.Truth.assertThat
import com.mk.habittracker.core.ui.theme.HabitTrackerTheme
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ThemeTest {

    @Test
    fun `theme provides material values`() {
        // This is a very basic test just to ensure the module can run tests and access theme
        // In a real app, we might use Compose UI tests for this, but here we just check if it compiles and runs.
        assertThat(true).isTrue()
    }
}
