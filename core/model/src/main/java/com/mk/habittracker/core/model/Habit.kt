package com.mk.habittracker.core.model

data class Habit(
    val id: String,
    val userId: String,
    val name: String,
    val createdAt: Long,
    val tagId: ByteArray? = null,
) {
    fun toMap(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "user_id" to userId,
            "name" to name,
            "created_at" to createdAt,
            "tag_id" to tagId,
        )

    companion object {
        fun from(
            id: String,
            data: Map<String, Any>?,
        ): Habit {
            data!!
            return Habit(
                id = id,
                userId = data["user_id"] as? String ?: error("couldn't read user_id"),
                name = data["name"] as? String ?: error("couldn't read name"),
                createdAt = data["created_at"] as? Long ?: error("couldn't read created_at"),
                tagId = data["tag_id"] as? ByteArray,
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Habit

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

data class HabitStats(
    val currentStreak: Int,
    val bestStreak: Int,
    val totalCheckIns: Int,
)

data class HabitDetail(
    val habit: Habit,
    val stats: HabitStats,
)
