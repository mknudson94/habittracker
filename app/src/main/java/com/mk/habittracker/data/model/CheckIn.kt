package com.mk.habittracker.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "check_in")
data class CheckIn(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "habit_id") val habitId: String,
    @ColumnInfo(name = "completed_date") val completedDate: LocalDate,
)
