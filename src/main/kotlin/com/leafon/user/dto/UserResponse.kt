package com.leafon.user.dto

import java.time.OffsetDateTime
import java.util.UUID

data class UserResponse(
    val id: UUID?,
    val supabaseUserId: UUID?,
    val email: String,
    val name: String?,
    val createdAt: OffsetDateTime?,
    val updatedAt: OffsetDateTime?,
    val lastLoginAt: OffsetDateTime?,
)
