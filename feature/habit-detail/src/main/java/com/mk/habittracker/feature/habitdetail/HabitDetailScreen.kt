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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jakewharton.threetenabp.AndroidThreeTen
import com.mk.habittracker.core.model.CheckIn
import com.mk.habittracker.core.model.Habit
import com.mk.habittracker.core.model.HabitDetail
import com.mk.habittracker.core.model.HabitStats
import com.mk.habittracker.core.ui.FullSheet
import com.mk.habittracker.feature.pairnfc.NfcPairingBottomSheet
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentMap
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

@Composable
fun HabitDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    vm: HabitDetailViewModel = hiltViewModel(),
) {
    val habitDetail by vm.habitDetail.collectAsStateWithLifecycle()
    val checkIns by vm.checkIns.collectAsStateWithLifecycle()
    habitDetail?.let {
        HabitDetailScreen(
            modifier = modifier,
            habitDetail = it,
            checkIns = checkIns.toPersistentList(),
            onBack = onBack,
        )
    } ?: Text("error fetching habit with id ${vm.habitId}")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitDetail: HabitDetail,
    onBack: () -> Unit,
    checkIns: ImmutableList<CheckIn>,
    modifier: Modifier = Modifier,
) {
    val nfcBottomSheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    FullSheet(
        modifier = modifier,
        title = habitDetail.habit.name,
        onBack = onBack,
    ) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OverviewSection(
                currentStreak = habitDetail.stats.currentStreak,
                bestStreak = habitDetail.stats.bestStreak,
                totalCheckIns = habitDetail.stats.totalCheckIns,
                createdAt = habitDetail.habit.createdAt,
            )
            NfcSection(
                isPaired = habitDetail.habit.tagId != null,
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
            habitId = habitDetail.habit.id,
            sheetState = nfcBottomSheetState,
            onDismiss = { showBottomSheet = false },
        )
    }
}

@Composable
private fun OverviewSection(
    currentStreak: Int,
    bestStreak: Int,
    totalCheckIns: Int,
    createdAt: Long,

) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "CURRENT STREAK",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currentStreak.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("days", style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.width(12.dp))
            Row(modifier = Modifier.weight(1f)) {
                Column {
                    Text("BEST", style = MaterialTheme.typography.labelMedium)
                    Text("TOTAL", style = MaterialTheme.typography.labelMedium)
                    Text("CREATED", style = MaterialTheme.typography.labelMedium)
                }
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        bestStreak.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        totalCheckIns.toString(),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    // todo: date format
                    val createdString =
                        Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault())
                            .format(org.threeten.bp.format.DateTimeFormatter.ofPattern("MMM dd"))
                    Text(
                        createdString,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun NfcSection(
    isPaired: Boolean,
    onClick: () -> Unit
) {
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
            if (isPaired) {
                NfcBodyPaired()
            } else {
                NfcBodyNotPaired()
            }
        }
    }
}

@Composable
private fun NfcBodyPaired() {
    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter = painterResource(R.drawable.outline_check_circle_24),
                contentDescription = "",
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "NFC tag paired",
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }

}

@Composable
private fun NfcBodyNotPaired() {
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
private fun HabitDetailScreenPreview(
    @PreviewParameter(IsPairedProvider::class) isPaired: Boolean,
) {
    AndroidThreeTen.init(LocalContext.current)
    HabitDetailScreen(
        habitDetail = HabitDetail(
            habit = Habit(
                id = "",
                userId = "",
                name = "Drink 8 glasses of water",
                createdAt = 1778557600000L,
                tagId = if (isPaired) byteArrayOf(1, 2, 3) else null,
            ),
            HabitStats(
                currentStreak = 12,
                bestStreak = 19,
                totalCheckIns = 59,
            )
        ),
        checkIns = previewCheckIns.toPersistentList(),
        onBack = {},
    )
}

class IsPairedProvider: PreviewParameterProvider<Boolean> {
    override val values: Sequence<Boolean> = sequenceOf(
        true,
        false
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
