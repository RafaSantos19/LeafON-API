package com.leafon.user.controller

import com.leafon.common.config.SecurityConfig
import com.leafon.user.dto.CreateUserRequest
import com.leafon.user.dto.UpdateUserRequest
import com.leafon.user.dto.UserResponse
import com.leafon.user.mapper.toResponse
import com.leafon.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/users")
@Tag(name = "Usuarios", description = "Consulta e gerenciamento do perfil do usuario autenticado.")
@SecurityRequirement(name = "bearerAuth")
class UserController(
    private val userService: UserService,
) {
    private val logger = LoggerFactory.getLogger(UserController::class.java)

    @GetMapping
    @Operation(summary = "Listar usuarios")
    fun findAll(): List<UserResponse> {
        return userService.findAll().map { it.toResponse() }
    }

    @GetMapping("/me")
    @Operation(summary = "Buscar perfil do usuario autenticado")
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
    @Operation(summary = "Buscar usuario por ID")
    fun findById(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): UserResponse {
        return userService.findOwnedById(id, uid).toResponse()
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar perfil do usuario autenticado")
    fun create(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @Valid @RequestBody request: CreateUserRequest,
    ): UserResponse {
        return userService.createAuthenticated(uid, request).toResponse()
    }

    @PutMapping("/me")
    @Operation(summary = "Atualizar perfil do usuario autenticado")
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
    @Operation(summary = "Atualizar usuario por ID")
    fun update(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @Valid @RequestBody request: UpdateUserRequest,
    ): UserResponse {
        return userService.updateOwnedUser(id, uid, request).toResponse()
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir perfil do usuario autenticado")
    fun deleteCurrentUser(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ) {
        userService.deleteCurrentUser(uid)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir usuario por ID")
    fun delete(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ) {
        userService.deleteOwnedUser(id, uid)
    }
}
