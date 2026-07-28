package com.mk.habittracker.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mk.habittracker.core.model.Habit

@Entity(tableName = "habit")
data class HabitEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
)

fun HabitEntity.asExternalModel() = Habit(
    id = id,
    userId = userId,
    name = name,
    createdAt = createdAt
)

fun Habit.asEntity() = HabitEntity(
    id = id,
    userId = userId,
    name = name,
    createdAt = createdAt
)
