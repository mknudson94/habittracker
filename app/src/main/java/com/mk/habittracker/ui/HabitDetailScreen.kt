package com.mk.habittracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mk.habittracker.R

@Composable
fun HabitDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: HabitDetailViewModel = hiltViewModel(),
) {
    val habit by vm.habit.collectAsStateWithLifecycle()
    val nCheckIns by vm.nCheckIns.collectAsStateWithLifecycle()
    habit?.let {
        HabitDetailScreen(
            modifier = modifier,
            habitId = vm.habitId,
            title = it.name,
            nCheckIns = nCheckIns,
            onBack = onBack,
        )
    } ?: Text("error fetching habit with id ${vm.habitId}")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: String,
    title: String,
    onBack: () -> Unit,
    nCheckIns: String,
    modifier: Modifier = Modifier,
) {
    val nfcBottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    FullSheet(
        modifier = modifier,
        title = title,
        onBack = onBack,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            NfcSection(
                onClick = { showBottomSheet = true },
            )
            HistorySection(nCheckIns = nCheckIns)
        }
    }
    if (showBottomSheet) {
        NfcPairingBottomSheet(
            habitId = habitId,
            sheetState = nfcBottomSheetState,
            onDismiss = { showBottomSheet = false },
        )
    }
}

@Composable
private fun NfcSection(onClick: () -> Unit) {
    Card(
        modifier =
            Modifier
                .clickable(onClick = onClick)
                .fillMaxWidth(),
    ) {
        Text("PHYSICAL TRIGGER", style = MaterialTheme.typography.titleMedium)
        Row {
            Icon(
                painter = painterResource(R.drawable.outline_add_link_24),
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
private fun HistorySection(nCheckIns: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text("HISTORY", style = MaterialTheme.typography.titleMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_history_24),
                contentDescription = "history icon",
            )
            Text("Completed $nCheckIns times")
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun HabitDetailScreenPreview() {
    HabitDetailScreen(
        habitId = "",
        title = "Drink 8 glasses of water",
        nCheckIns = "2",
        onBack = {},
    )
}
