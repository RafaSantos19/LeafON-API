package com.leafon.user.controller

import com.leafon.common.config.SecurityConfig
import com.leafon.user.dto.CreateUserRequest
import com.leafon.user.dto.UpdateUserRequest
import com.leafon.user.dto.UserResponse
import com.leafon.user.mapper.toResponse
import com.leafon.user.service.UserService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {
    private val logger = LoggerFactory.getLogger(UserController::class.java)

    @GetMapping
    fun findAll(): List<UserResponse> {
        return userService.findAll().map { it.toResponse() }
    }

    @GetMapping("/me")
    fun findCurrentUser(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): UserResponse {
        logger.info("Temporary users/me GET received with authenticatedUid={}", uid)

        val user = userService.findCurrentUser(uid)

        logger.info(
            "Temporary users/me GET resolved authenticatedUid={} to localUserId={} email={}",
            uid,
            user.id,
            user.email,
        )

        return user.toResponse()
    }

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): UserResponse {
        return userService.findOwnedById(id, uid).toResponse()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @Valid @RequestBody request: CreateUserRequest,
    ): UserResponse {
        return userService.createAuthenticated(uid, request).toResponse()
    }

    @PutMapping("/me")
    fun updateCurrentUser(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @Valid @RequestBody request: UpdateUserRequest,
    ): UserResponse {
        logger.info(
            "Temporary users/me PUT received with authenticatedUid={} payload={emailPresent={}, namePresent={}, phonePresent={}}",
            uid,
            request.email != null,
            request.name != null,
            request.phone != null,
        )

        val user = userService.updateCurrentUser(uid, request)

        logger.info(
            "Temporary users/me PUT updated authenticatedUid={} localUserId={} email={}",
            uid,
            user.id,
            user.email,
        )

        return user.toResponse()
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @Valid @RequestBody request: UpdateUserRequest,
    ): UserResponse {
        return userService.updateOwnedUser(id, uid, request).toResponse()
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCurrentUser(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ) {
        userService.deleteCurrentUser(uid)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ) {
        userService.deleteOwnedUser(id, uid)
    }
}
