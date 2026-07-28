package com.mk.habittracker.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import com.mk.habittracker.core.model.Habit

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
        onHabitClick = onHabitClick,
        onLogout = onLogout,
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun MainScreen(
    habits: ImmutableList<Habit>,
    modifier: Modifier = Modifier,
    onAddHabit: () -> Unit = {},
    onHabitClick: (habitId: String) -> Unit = {},
    onLogout: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            MediumFlexibleTopAppBar(
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
                    .fillMaxSize(),
        ) {
            habits.forEach { habit ->
                key(habit.id) {
                    HabitRow(habit = habit, onClick = { onHabitClick(habit.id) })
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MainScreen(
        habits = persistentListOf(),
    )
}
