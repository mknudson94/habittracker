package com.mk.habittracker.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mk.habittracker.core.model.Habit
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
            HabitCard(
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
                    .verticalScroll(rememberScrollState())
                    .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.hero_doodle),
                    contentDescription = "",
                )
                val config = LocalConfiguration.current
                val locale =
                    ConfigurationCompat.getLocales(config).get(0)
                        ?: LocaleListCompat.getDefault()[0]!!
                val dateString = LocalDate.now().format(
                    DateTimeFormatter.ofPattern("EEEE, MMMM dd", locale),
                )
                Column(Modifier.padding(start = 8.dp)) {
                    Text(
                        text = dateString.uppercase(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "0 of 3 habits done today",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
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
            HabitCard(
                habit = habit,
                checkIns = persistentListOf(),
                onClick = {},
                onToggleCheckIn = { _, _ -> },
            )
        }
    )
}
