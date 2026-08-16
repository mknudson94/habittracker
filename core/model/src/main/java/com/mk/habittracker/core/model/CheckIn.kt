package com.mk.habittracker.core.model

import java.time.LocalDate

data class CheckIn(
    val id: String,
    val habitId: String,
    val userId: String,
    val completedDate: LocalDate,
    val nfcUid: ByteArray? = null,
) {
    fun toMap(): Map<String, Any?> =
        mapOf(
            "id" to id,
            "habit_id" to habitId,
            "user_id" to userId,
            "completed_date" to completedDate.toString(),
            "nfc_tag_uid" to nfcUid?.map { it.toInt() },
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CheckIn

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

    companion object {
        fun from(
            id: String,
            data: Map<String, Any>?,
        ): CheckIn {
            data!!
            return CheckIn(
                id = id,
                habitId = data["habit_id"] as? String ?: error("couldn't read habit_id"),
                userId = data["user_id"] as? String ?: error("couldn't read user_id"),
                completedDate =
                    LocalDate.parse(
                        data["completed_date"] as? String ?: error("couldn't read completed_date"),
                    ),
                nfcUid =
                    (data["nfc_tag_uid"] as? List<*>)
                        ?.map { (it as Number).toByte() }
                        ?.toByteArray(),
            )
        }
    }
}
