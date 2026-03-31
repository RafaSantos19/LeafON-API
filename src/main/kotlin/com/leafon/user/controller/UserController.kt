package com.leafon.user.controller

import com.leafon.user.dto.CreateUserRequest
import com.leafon.user.dto.UpdateUserRequest
import com.leafon.user.dto.UserResponse
import com.leafon.user.entity.User
import com.leafon.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {
    @GetMapping("/{id}")
    fun findById(@PathVariable id: UUID): ResponseEntity<UserResponse> {
        val user = userService.findById(id)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(user.toResponse())
    }

    @PostMapping
    fun create(@Valid @RequestBody request: CreateUserRequest): ResponseEntity<UserResponse> {
        val createdUser = userService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser.toResponse())
    }

    @PutMapping("/{id}")
    fun update(@PathVariable id: UUID, @Valid @RequestBody request: UpdateUserRequest): ResponseEntity<UserResponse> {
        val updatedUser = userService.update(id, request)
            ?: return ResponseEntity.notFound().build()

        return ResponseEntity.ok(updatedUser.toResponse())
    }

    private fun User.toResponse(): UserResponse =
        UserResponse(
            id = id,
            supabaseUserId = supabaseUserId,
            email = email,
            name = name,
            createdAt = createdAt,
            updatedAt = updatedAt,
            lastLoginAt = lastLoginAt,
        )
}
