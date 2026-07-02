package mk.habittracker.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onAddHabit: () -> Unit = {},
    onHabitClick: (habitId: Int) -> Unit = {},
    vm: MainScreenViewModel = hiltViewModel(),
) {
    val habits by vm.habits.collectAsStateWithLifecycle()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddHabit
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "add habit"
                )
            }
        }
    ) { paddingValues ->
        Column(modifier = modifier
            .padding(paddingValues)
            .fillMaxSize()) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Habits",
                style = MaterialTheme.typography.displayMedium
            )
            habits.forEach { habit ->
                HabitRow(habit = habit, onClick = { onHabitClick(habit.id) })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    MainScreen()
}
