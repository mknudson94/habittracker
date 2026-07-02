package mk.habittracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mk.habittracker.data.model.Habit
import java.time.LocalDate
import kotlin.random.Random

@Composable
fun HabitRow(
    habit: Habit,
    onClick: () -> Unit = {},
    vm: MainScreenViewModel = hiltViewModel()
) {
    val checkIns by vm.getCheckIns(
        habitId = habit.id
    ).collectAsStateWithLifecycle()
    Column(modifier = Modifier.clickable(onClick = onClick)) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = habit.name,
            )
            Checkbox(
                checked = checkIns.any { it.completedDate == LocalDate.now() },
                onCheckedChange = {}
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("S", "M", "T", "W", "Th", "F", "Sa").forEach {
                Text(it, style = MaterialTheme.typography.labelSmall)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            repeat(7) { i ->
                val day = LocalDate.now().minusDays(6L - i)
                Icon(
                    imageVector = if (checkIns.any { it.completedDate == day }) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.Clear
                    },
                    contentDescription = "checked",
                )
            }
        }
    }
}

@Composable
fun HabitRow() {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = "Drink 8 glasses of water",
            )
            Checkbox(
                checked = false,
                onCheckedChange = {}
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("S", "M", "T", "W", "Th", "F", "Sa").forEach {
                Text(it, style = MaterialTheme.typography.labelSmall)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            repeat(7) {
                Icon(
                    imageVector = if (Random.nextBoolean()) {
                        Icons.Default.CheckCircle
                    } else {
                        Icons.Default.Clear
                    },
                    contentDescription = "checked",
                )
            }
        }
    }
}