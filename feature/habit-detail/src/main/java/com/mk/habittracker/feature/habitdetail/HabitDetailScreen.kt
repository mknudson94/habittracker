package com.mk.habittracker.feature.habitdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mk.habittracker.core.model.CheckIn
import com.mk.habittracker.core.ui.FullSheet
import com.mk.habittracker.feature.pairnfc.NfcPairingBottomSheet
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import java.time.temporal.WeekFields

@Composable
fun HabitDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: HabitDetailViewModel = hiltViewModel(),
) {
    val habit by vm.habit.collectAsStateWithLifecycle()
    val checkIns by vm.checkIns.collectAsStateWithLifecycle()
    val nCheckIns by vm.nCheckIns.collectAsStateWithLifecycle()
    habit?.let {
        HabitDetailScreen(
            modifier = modifier,
            habitId = vm.habitId,
            title = it.name,
            checkIns = checkIns.toPersistentList(),
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
    checkIns: ImmutableList<CheckIn>,
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
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            NfcSection(
                onClick = { showBottomSheet = true },
            )
            // HistorySection(nCheckIns = nCheckIns)
            // CheckInHeatmap(checkIns = checkIns)
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier =
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CheckInWeeklyCompletions(
                        checkInsByWeek =
                            checkIns
                                .groupingBy { it.completedDate.get(WeekFields.ISO.weekOfYear()) }
                                .eachCount()
                                .toPersistentMap(),
                    )
                }
            }
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
    ElevatedCard(
        modifier =
            Modifier
                .clickable(onClick = onClick)
                .fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "PHYSICAL TRIGGER",
                style = MaterialTheme.typography.labelLarge,
            )
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f)
                            .drawBehind {
                                drawCircle(
                                    color = Color.Black,
                                    alpha = .7f,
                                    style =
                                        Stroke(
                                            width = 1.dp.toPx(),
                                            pathEffect =
                                                PathEffect.dashPathEffect(
                                                    floatArrayOf(8f, 8f),
                                                    0f,
                                                ),
                                        ),
                                )
                            },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.outline_add_link_24),
                        contentDescription = "add nfc tag",
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Add NFC tag",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Tap to pair a physical object",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun HistorySection(nCheckIns: String) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
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
        checkIns = previewCheckIns.toPersistentList(),
        nCheckIns = "2",
        onBack = {},
    )
}

private val previewDates =
    listOf(
        "2026-06-15",
        "2026-06-16",
        "2026-06-18",
        "2026-06-20",
        "2026-06-21",
        "2026-06-23",
        "2026-06-24",
        "2026-06-27",
        "2026-06-28",
        "2026-06-29",
        "2026-07-01",
        "2026-07-02",
        "2026-07-04",
        "2026-07-05",
        "2026-07-12",
        "2026-07-14",
        "2026-07-15",
        "2026-07-17",
        "2026-07-18",
        "2026-07-20",
        "2026-07-22",
        "2026-07-23",
        "2026-07-25",
        "2026-07-26",
        "2026-07-28",
        "2026-07-29",
        "2026-07-30",
        "2026-08-01",
        "2026-08-02",
    )

private val previewCheckIns = previewDates.map { checkIn(it) }
