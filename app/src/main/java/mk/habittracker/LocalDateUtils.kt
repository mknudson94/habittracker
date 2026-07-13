package mk.habittracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import mk.habittracker.ui.DAYS_IN_WEEK
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data object LocalDateUtils {
    @Composable
    fun previousSevenDaysLabels(): Array<String> {
        val config = LocalConfiguration.current
        val locale =
            ConfigurationCompat.getLocales(config).get(0)
                ?: LocaleListCompat.getDefault()[0]!!

        return previousSevenDaysLabels(locale)
    }

    fun previousSevenDaysLabels(locale: Locale): Array<String> {
        val today = LocalDate.now()
        val labels = arrayOfNulls<String>(DAYS_IN_WEEK)
        repeat(DAYS_IN_WEEK) { i ->
            val day = today.minusDays(DAYS_IN_WEEK - (i + 1L))
            labels[i] = day.dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, locale)
        }
        return labels as Array<String>
    }
}
