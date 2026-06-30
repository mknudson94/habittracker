package mk.habittracker.data.util

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.ZoneId

public class Converters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromZoneId(value: ZoneId?): String? = value?.toString()

    @TypeConverter
    fun toZoneId(value: String?): ZoneId? = value?.let { ZoneId.of(it) }
}