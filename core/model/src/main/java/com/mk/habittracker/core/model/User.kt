package com.mk.habittracker.core.model

import java.time.ZoneId

data class User(
    val id: String,
    val email: String,
    val createdAt: Long,
    val userTimezone: ZoneId,
)
