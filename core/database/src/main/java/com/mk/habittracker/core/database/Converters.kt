package com.mk.habittracker.core.database

import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.ZoneId

class Converters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromZoneId(value: ZoneId?): String? = value?.toString()

    @TypeConverter
    fun toZoneId(value: String?): ZoneId? = value?.let { ZoneId.of(it) }
}
