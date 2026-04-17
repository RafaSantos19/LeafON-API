package com.leafon.user.controller

import com.leafon.user.dto.CreateUserRequest
import com.leafon.user.dto.UpdateUserRequest
import com.leafon.user.dto.UserResponse
import com.leafon.user.entity.User
import com.leafon.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {

    @GetMapping
    fun findAll(): List<UserResponse> {
        return userService.findAll().map { it.toResponse() }
    }

    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): UserResponse {
        return userService.findById(id).toResponse()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateUserRequest): UserResponse {
        return userService.create(request).toResponse()
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @Valid @RequestBody request: UpdateUserRequest,
    ): UserResponse {
        return userService.update(id, request).toResponse()
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: UUID) {
        userService.delete(id)
    }

    private fun User.toResponse(): UserResponse =
        UserResponse(
            id = id,
            email = email,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt,
        )
}