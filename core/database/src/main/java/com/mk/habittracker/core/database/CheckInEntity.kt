package com.mk.habittracker.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import com.mk.habittracker.core.model.CheckIn
import java.time.LocalDate

// TODO: maybe store julian days for better indexing/sorting
@Entity(
    tableName = "check_in",
    primaryKeys = ["id", "completed_date"],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = arrayOf("id"),
            childColumns = arrayOf("habit_id"),
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class CheckInEntity(
    val id: String,
    @ColumnInfo(name = "habit_id") val habitId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "completed_date") val completedDate: LocalDate,
    @ColumnInfo(name = "nfc_tag_uid", typeAffinity = ColumnInfo.BLOB) val nfcUid: ByteArray? = null,
)

fun CheckInEntity.asExternalModel() =
    CheckIn(
        id = id,
        habitId = habitId,
        userId = userId,
        completedDate = completedDate,
        nfcUid = nfcUid,
    )

fun CheckIn.asEntity() =
    CheckInEntity(
        id = id,
        habitId = habitId,
        userId = userId,
        completedDate = completedDate,
        nfcUid = nfcUid,
    )
