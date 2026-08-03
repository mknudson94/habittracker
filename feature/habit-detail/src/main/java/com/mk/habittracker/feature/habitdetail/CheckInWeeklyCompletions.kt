package com.mk.habittracker.feature.habitdetail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import java.time.LocalDate
import java.time.Year
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields

private const val DAY_HEIGHT_DP = 16

@Composable
fun CheckInWeeklyCompletions(
    checkInsByWeek: PersistentMap<Int, Int>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Weekly completions - last 12 weeks")
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.spacedBy(4.dp),
//            verticalAlignment = Alignment.Bottom,
//        ) {
//            val currentWeek = LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear())
//            for (weekNumber in (currentWeek - 11) .. currentWeek) {
//                Box(
//                    modifier = Modifier
//                        .size(
//                            width = 12.dp,
//                            height = ((checkInsByWeek[weekNumber] ?: 0) * DAY_HEIGHT_DP).dp
//                        )
//                        .background(
//                            color = MaterialTheme.colorScheme.primary,
//                            shape = RoundedCornerShape(2.dp),
//                        )
//                )
//            }
//        }
        val color = MaterialTheme.colorScheme.primary
        Canvas(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height((DAY_HEIGHT_DP * 8).dp)
        ) {
            val width = size.width / 12
            val margin = size.width / 48
            val currentWeek = LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear())
            var x = 0f
            for (weekNumber in (currentWeek - 11)..currentWeek) {
                checkInsByWeek[weekNumber]?.let {
                    val barHeight = (it * DAY_HEIGHT_DP).dp.toPx()
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(
                            x = x + margin,
                            y = size.height - barHeight
                        ),
                        size = Size(
                            width = width - (margin * 2),
                            height = barHeight,
                        ),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                } ?: this.drawOval(
                    color = color,
                    topLeft = Offset(x + margin, size.height - 3),
                    size = Size(width - (margin * 2), 4f)
                )
                x += width
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val currentWeek = LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear())
            for (weekNumber in (currentWeek - 12)..currentWeek step 4) {
                Text(
                    text = firstDayOfWeek(weekNumber)
                        .format(DateTimeFormatter.ofPattern("MMM dd")),
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckInWeeklyCompletionsPreview() {
    CheckInWeeklyCompletions(
        checkInsByWeek = previewCheckInsByWeek
    )
}

val previewCheckInsByWeek = persistentMapOf(
    20 to 7,
    22 to 4,
    23 to 1,
    24 to 7,
    25 to 5,
    27 to 7,
    28 to 5,
    29 to 2,
    30 to 7,
    31 to 4,
)


fun firstDayOfWeek(weekNumber: Int): LocalDate {
    val weekFields = WeekFields.ISO // Monday is the first day of the week
    val currentYear = Year.now().value

    return LocalDate.of(currentYear, 1, 4) // Jan 4th is always in ISO week 1
        .with(weekFields.weekOfYear(), weekNumber.toLong())
        .with(weekFields.dayOfWeek(), 1L)
}
