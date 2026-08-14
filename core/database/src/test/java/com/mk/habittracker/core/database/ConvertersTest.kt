package com.mk.habittracker.core.database

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun `LocalDate conversion works`() {
        val date = LocalDate.of(2025, 8, 14)
        val string = converters.fromLocalDate(date)
        assertThat(string).isEqualTo("2025-08-14")
        assertThat(converters.toLocalDate(string)).isEqualTo(date)
    }

    @Test
    fun `LocalDate null conversion works`() {
        assertThat(converters.fromLocalDate(null)).isNull()
        assertThat(converters.toLocalDate(null)).isNull()
    }

    @Test
    fun `ZoneId conversion works`() {
        val zoneId = ZoneId.of("America/Los_Angeles")
        val string = converters.fromZoneId(zoneId)
        assertThat(string).isEqualTo("America/Los_Angeles")
        assertThat(converters.toZoneId(string)).isEqualTo(zoneId)
    }

    @Test
    fun `ZoneId null conversion works`() {
        assertThat(converters.fromZoneId(null)).isNull()
        assertThat(converters.toZoneId(null)).isNull()
    }
}
