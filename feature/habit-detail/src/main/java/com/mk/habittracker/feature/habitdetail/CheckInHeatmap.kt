package com.mk.habittracker.feature.habitdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import com.jakewharton.threetenabp.AndroidThreeTen
import com.mk.habittracker.core.model.CheckIn
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

private const val DAYS_IN_WEEK = 7

@Composable
fun CheckInHeatmap(
    checkIns: ImmutableList<CheckIn>,
) {
    val config = LocalConfiguration.current
    val locale =
        ConfigurationCompat.getLocales(config).get(0)
            ?: LocaleListCompat.getDefault()[0]!!

    val currentDate = LocalDate.now()
    val firstOfCurrentMonth = currentDate.withDayOfMonth(1)
    val daysInMonth = firstOfCurrentMonth.lengthOfMonth()
    val monthDates = (0 until daysInMonth).map { firstOfCurrentMonth.plusDays(it.toLong()) }

    // DayOfWeek.value returns 1 (Monday) to 7 (Sunday).
    // Since our headerRow starts with Monday, we offset by (value - 1).
    val offset = firstOfCurrentMonth.dayOfWeek.value - 1

    LazyVerticalGrid(
        columns = GridCells.Fixed(DAYS_IN_WEEK),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        headerRow(locale = locale)
        repeat(offset) {
            item { }
        }
        items(monthDates) { date ->
            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .background(
                        color = if (checkIns.any { it.completedDate.isEqual(date) }) {
                            Color.Green
                        } else {
                            Color.DarkGray
                        },
                        shape = CircleShape
                    )
            ) {
                Text(text = date.toString())
            }
        }
    }
}

private fun LazyGridScope.headerRow(
    locale: Locale,
) {
    DayOfWeek.entries.forEach {
        item {
            Text(
                text = it.getDisplayName(TextStyle.NARROW_STANDALONE, locale),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun CheckInHeatmapPreview() {
    AndroidThreeTen.init(LocalContext.current)
    Column {
        Row {
            previewCheckIns.forEach {
                Text(it.completedDate.toString())
            }
        }
        CheckInHeatmap(
            checkIns = previewCheckIns
        )
    }
}

internal fun checkIn(
    id: String,
    habitId: String = "habit-id",
    userId: String = "user-id",
    completedDate: LocalDate,
) = CheckIn(
    id = id,
    habitId = habitId,
    userId = userId,
    completedDate = completedDate,
    nfcUid = null,
)

internal fun checkIn(
    isoDate: String
) = CheckIn(
    id = isoDate,
    habitId = "habit-id",
    userId = "user-id",
    completedDate = LocalDate.parse(isoDate),
    nfcUid = null,
)


private val previewCheckIns = LocalDate.now().let {
    persistentListOf(
        checkIn(
            id = "0",
            completedDate = it.plusDays(1),
        ),
        checkIn(
            id = "1",
            completedDate = it.plusDays(3),
        ),
        checkIn(
            id = "2",
            completedDate = it.plusDays(7),
        ),
        checkIn(
            id = "3",
            completedDate = it.plusDays(14),
        ),
    )
}
