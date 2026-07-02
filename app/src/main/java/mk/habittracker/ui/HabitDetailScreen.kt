package mk.habittracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    onBack: () -> Unit,
    title: String,
    nCheckIns: String,
) {
    val nfcBottomSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

    FullSheet(
        title = title,
        onBack = onBack,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NfcSection(
                onClick = { showBottomSheet = true }
            )
            HistorySection(nCheckIns = nCheckIns)
        }
    }
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = nfcBottomSheetState,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info, contentDescription = ""
                )
                Text("Ready to scan", style = MaterialTheme.typography.displaySmall)
                Text("Hold your phone near the NFC tag")
                TextButton(
                    onClick = { showBottomSheet = false },

                ) {
                    Text("Cancel")
                }
            }
        }
    }
}

@Composable
fun NfcSection(onClick: () -> Unit) {
    Card(modifier = Modifier
        .clickable(onClick = onClick)
        .fillMaxWidth()
    ) {
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