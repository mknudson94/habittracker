package mk.habittracker

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

data object LocalDateUtils {

    @Composable
    fun previousSevenDaysLabels(): Array<String> {
        val config = LocalConfiguration.current
        val locale = ConfigurationCompat.getLocales(config).get(0)
            ?: LocaleListCompat.getDefault()[0]!!

        return previousSevenDaysLabels(locale)
    }

    fun previousSevenDaysLabels(locale: Locale): Array<String> {
        val today = LocalDate.now()
        val labels = arrayOfNulls<String>(7)
        repeat(7) { i ->
            val day = today.minusDays(6L - i)
            labels[i] = day.dayOfWeek.getDisplayName(TextStyle.SHORT_STANDALONE, locale)
        }
        return labels as Array<String>
    }
}