package com.mk.habittracker.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mk.habittracker.core.model.Habit
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onAddHabit: () -> Unit = {},
    onHabitClick: (habitId: String) -> Unit = {},
    onLogout: () -> Unit = {},
    vm: MainScreenViewModel = hiltViewModel(),
) {
    val habits by vm.habits.collectAsStateWithLifecycle()
    MainScreen(
        habits = habits,
        modifier = modifier,
        onAddHabit = onAddHabit,
        onLogout = onLogout,
        habitRow = { habit ->
            HabitRow(
                habit = habit,
                onClick = { onHabitClick(habit.id) },
            )
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    habits: ImmutableList<Habit>,
    modifier: Modifier = Modifier,
    onAddHabit: () -> Unit = {},
    onLogout: () -> Unit = {},
    habitRow: @Composable (Habit) -> Unit,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            MediumTopAppBar(
                title = {
                    Text("Habits")
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            painterResource(R.drawable.log_out),
                            contentDescription = "log out",
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabit,
            ) {
                Icon(
                    painter = painterResource(R.drawable.outline_add_24),
                    contentDescription = "add habit",
                )
            }
        },
    ) { paddingValues ->
        Column(
            modifier =
                Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize(),
        ) {
            habits.forEach { habit ->
                key(habit.id) {
                    habitRow(habit)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MainScreen(
        habits = persistentListOf(
            Habit(
                id = "1",
                userId = "1",
                name = "Drink water",
                createdAt = 0L,
            ),
            Habit(
                id = "2",
                userId = "1",
                name = "Exercise",
                createdAt = 0L,
            )
        ),
        habitRow = { habit ->
            // Use a simple Text for preview to avoid ViewModel issues in nested components
            HabitRow(
                habit = habit,
                checkIns = persistentListOf(),
                onClick = {},
                onToggleCheckIn = { _, _ -> },
            )
        }
    )
}
