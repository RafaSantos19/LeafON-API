package com.leafon.user.dto

import jakarta.validation.constraints.Email

data class UpdateUserRequest(
    @field:Email
    val email: String?,

    val name: String?,
)
