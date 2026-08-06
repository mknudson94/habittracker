package com.mk.habittracker.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mk.habittracker.core.model.CheckIn
import java.time.LocalDate

@Entity(tableName = "check_in")
data class CheckInEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "habit_id") val habitId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "completed_date") val completedDate: LocalDate,
    @ColumnInfo(name = "nfc_tag_uid", typeAffinity = ColumnInfo.BLOB) val nfcUid: ByteArray? = null,
)

fun CheckInEntity.asExternalModel() = CheckIn(
    id = id,
    habitId = habitId,
    userId = userId,
    completedDate = completedDate,
    nfcUid = nfcUid
)

fun CheckIn.asEntity() = CheckInEntity(
    id = id,
    habitId = habitId,
    userId = userId,
    completedDate = completedDate,
    nfcUid = nfcUid
)
