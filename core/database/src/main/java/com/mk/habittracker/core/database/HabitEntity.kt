package com.mk.habittracker.core.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mk.habittracker.core.model.Habit

@Entity(
    tableName = "habit",
)
data class HabitEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "tag_id", typeAffinity = ColumnInfo.BLOB) val tagId: ByteArray?,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HabitEntity

        if (createdAt != other.createdAt) return false
        if (id != other.id) return false
        if (userId != other.userId) return false
        if (name != other.name) return false
        if (tagId != null) {
            if (other.tagId == null) return false
            if (!tagId.contentEquals(other.tagId)) return false
        } else if (other.tagId != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = createdAt.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (tagId?.contentHashCode() ?: 0)
        return result
    }
}

fun HabitEntity.asExternalModel() =
    Habit(
        id = id,
        userId = userId,
        name = name,
        createdAt = createdAt,
        tagId = tagId,
    )

fun Habit.asEntity() =
    HabitEntity(
        id = id,
        userId = userId,
        name = name,
        createdAt = createdAt,
        tagId = tagId,
    )
