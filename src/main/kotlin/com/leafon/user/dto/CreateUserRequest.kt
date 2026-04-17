package com.leafon.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateUserRequest(
    @field:Email
    @field:NotBlank
    val email: String,

    @field:NotBlank
    val name: String,
)
