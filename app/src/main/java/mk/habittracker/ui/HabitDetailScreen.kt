package mk.habittracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.test.history

@Composable
fun HabitDetailScreen(
    onBack: () -> Unit,
    vm: HabitDetailViewModel = hiltViewModel(),
) {
    val habit by vm.habit.collectAsStateWithLifecycle()
    val nCheckIns by vm.nCheckIns.collectAsStateWithLifecycle()
    habit?.let {
        HabitDetailScreen(
            onBack = onBack,
            title = it.name,
            nCheckIns = nCheckIns,
        )
    } ?: Text("error fetching habit with id ${vm.habitId}")
}

@Composable
fun HabitDetailScreen(
    onBack: () -> Unit,
    title: String,
    nCheckIns: String,
) {
    FullSheet(
        title = title,
        onBack = onBack,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NfcSection()
            HistorySection(nCheckIns = nCheckIns)
        }
    }
}

@Composable
fun NfcSection() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text("PHYSICAL TRIGGER", style = MaterialTheme.typography.titleMedium)
        Row {
            Icon(
                imageVector = Icons.Default.AddMissing,
                contentDescription = "add nfc tag",
            )
            Column {
                Text("Add NFC tag")
                Text("Tap to pair a physical object")
            }
        }
    }
}

@Composable
fun HistorySection(
    nCheckIns: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text("HISTORY", style = MaterialTheme.typography.titleMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.history, contentDescription = "history icon")
            Text("Completed $nCheckIns times")
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun HabitDetailScreenPreview() {
    HabitDetailScreen(
        onBack = {},
        title = "Drink 8 glasses of water",
        nCheckIns = "2",
    )
}