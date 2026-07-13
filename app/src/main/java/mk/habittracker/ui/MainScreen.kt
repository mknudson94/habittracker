package mk.habittracker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.habittracker.R
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import mk.habittracker.data.model.Habit

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onAddHabit: () -> Unit = {},
    onHabitClick: (habitId: String) -> Unit = {},
    vm: MainScreenViewModel = hiltViewModel(),
) {
    val habits by vm.habits.collectAsStateWithLifecycle()
    MainScreen(
        habits = habits,
        modifier = modifier,
        onAddHabit = onAddHabit,
        onHabitClick = onHabitClick,
    )
}

@Composable
fun MainScreen(
    habits: ImmutableList<Habit>,
    modifier: Modifier = Modifier,
    onAddHabit: () -> Unit = {},
    onHabitClick: (habitId: String) -> Unit = {},
) {
    Scaffold(
        modifier = modifier,
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
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Habits",
                style = MaterialTheme.typography.displayMedium,
            )
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
