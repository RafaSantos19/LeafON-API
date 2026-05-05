package com.leafon.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class UpdateUserRequest(

    @field:Email
    val email: String?,

    @field:NotBlank
    @field:Size(min = 2, max = 100)
    val name: String?,

    @field:NotBlank
    val phone: String?,
)
