package com.mk.habittracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mk.habittracker.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.mk.habittracker.LocalDateUtils
import com.mk.habittracker.data.model.CheckIn
import com.mk.habittracker.data.model.Habit
import java.time.LocalDate

public const val DAYS_IN_WEEK = 7

@Composable
internal fun HabitRow(
    habit: Habit,
    onClick: () -> Unit = {},
    vm: MainScreenViewModel = hiltViewModel(),
) {
    // since getCheckIns is a function that creates a flow (not a flow), we need to
    // call it once inside a remember block
    val checkInsFlow =
        remember(habit.id) {
            vm.getCheckIns(habit.id)
        }
    val checkIns by checkInsFlow.collectAsStateWithLifecycle()

    HabitRow(
        habit = habit,
        checkIns = checkIns,
        onClick = onClick,
        onToggleCheckIn = vm::toggleCheckIn,
    )
}

@Composable
internal fun HabitRow(
    habit: Habit,
    checkIns: ImmutableList<CheckIn>,
    onClick: () -> Unit = {},
    onToggleCheckIn: (isChecked: Boolean, habitId: String) -> Unit = { _, _ -> },
) {
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = habit.name,
            )
            Checkbox(
                checked = checkIns.any { it.completedDate == LocalDate.now() },
                onCheckedChange = { isChecked ->
                    onToggleCheckIn(isChecked, habit.id)
                },
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            LocalDateUtils.previousSevenDaysLabels().forEach {
                Text(it, style = MaterialTheme.typography.labelSmall)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            repeat(DAYS_IN_WEEK) { i ->
                val day = LocalDate.now().minusDays(DAYS_IN_WEEK - (i + 1L))
                Icon(
                    painter =
                        painterResource(
                            if (checkIns.any { it.completedDate == day }) {
                                R.drawable.outline_check_circle_24
                            } else {
                                R.drawable.outline_close_24
                            },
                        ),
                    contentDescription = "checked",
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun HabitRowPreview() {
    HabitRow(
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
                CheckIn(
                    id = "1",
                    habitId = "1",
                    userId = "1",
                    completedDate = LocalDate.now().minusDays(3),
                ),
                CheckIn(
                    id = "1",
                    habitId = "1",
                    userId = "1",
                    completedDate = LocalDate.now().minusDays(4),
                ),
            ),
        onClick = {},
    )
}
