package com.leafon.user.mapper

import com.leafon.user.dto.UserResponse
import com.leafon.user.entity.User

fun User.toResponse() = UserResponse(
    id = id,
    email = email,
    name = name,
    createdAt = createdAt,
    updatedAt = updatedAt
)