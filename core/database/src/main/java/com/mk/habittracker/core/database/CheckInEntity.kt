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
    @ColumnInfo(name = "habit_id", index = true) val habitId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "completed_date") val completedDate: LocalDate,
    @ColumnInfo(name = "nfc_tag_uid", typeAffinity = ColumnInfo.BLOB) val nfcUid: ByteArray? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CheckInEntity

        if (id != other.id) return false
        if (habitId != other.habitId) return false
        if (userId != other.userId) return false
        if (completedDate != other.completedDate) return false
        if (!nfcUid.contentEquals(other.nfcUid)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + habitId.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + completedDate.hashCode()
        result = 31 * result + (nfcUid?.contentHashCode() ?: 0)
        return result
    }
}

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
