package com.mk.habittracker.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mk.habittracker.core.common.DAYS_IN_WEEK
import com.mk.habittracker.core.model.CheckIn
import com.mk.habittracker.core.model.Habit
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate
import java.time.format.TextStyle

@Composable
internal fun HabitCard(
    habit: Habit,
    onClick: () -> Unit = {},
    vm: MainScreenViewModel = hiltViewModel(),
) {
    val checkInsFlow =
        remember(habit.id) {
            vm.getCheckIns(habit.id)
        }
    val checkIns by checkInsFlow.collectAsStateWithLifecycle()

    HabitCard(
        habit = habit,
        checkIns = checkIns,
        onClick = onClick,
        onToggleCheckIn = vm::toggleCheckIn,
    )
}

@Composable
internal fun HabitCard(
    habit: Habit,
    checkIns: ImmutableList<CheckIn>,
    onClick: () -> Unit = {},
    onToggleCheckIn: (isChecked: Boolean, habitId: String) -> Unit = { _, _ -> },
) {
    val config = LocalConfiguration.current
    val locale =
        ConfigurationCompat.getLocales(config).get(0)
            ?: LocaleListCompat.getDefault()[0]!!

    ElevatedCard {
        Column(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = habit.name,
                    style = MaterialTheme.typography.titleLarge
                )
                Checkbox(
                    checked = checkIns.any { it.completedDate == LocalDate.now() },
                    onCheckedChange = { isChecked ->
                        onToggleCheckIn(isChecked, habit.id)
                    },
                )
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(DAYS_IN_WEEK) { i ->
                    val day = LocalDate.now().minusDays(DAYS_IN_WEEK - (i + 1L))
                    val dayLabel = day.dayOfWeek.getDisplayName(TextStyle.NARROW_STANDALONE, locale)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(dayLabel, style = MaterialTheme.typography.labelSmall)
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(12.dp)
                                .background(
                                    color = if (checkIns.any { it.completedDate == day }) {
                                        Color(0xff5cb85c)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceDim
                                    },
                                    shape = CircleShape,
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun HabitCardPreview() {
    Box(
        modifier = Modifier.padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        HabitCard(
            habit =
                Habit(
                    id = "1",
                    userId = "1",
                    name = "Brush teeth",
                    createdAt = 12345L,
                ),
            checkIns =
                persistentListOf(
                    CheckIn(
                        id = "1",
                        habitId = "1",
                        userId = "1",
                        completedDate = LocalDate.now().minusDays(1),
                    ),
                ),
            onClick = {},
        )
    }
}
