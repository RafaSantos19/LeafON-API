package com.leafon.user.controller

import com.leafon.auth.security.BearerTokenExtractor
import com.leafon.common.config.SecurityConfig
import com.leafon.common.exception.UnauthorizedException
import com.leafon.user.dto.CreateUserRequest
import com.leafon.user.dto.UpdateUserRequest
import com.leafon.user.dto.UserResponse
import com.leafon.user.mapper.toResponse
import com.leafon.user.service.UserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/users")
@Tag(name = "Usuarios", description = "Consulta e gerenciamento do perfil do usuario autenticado.")
@SecurityRequirement(name = "bearerAuth")
class UserController(
    private val userService: UserService,
    private val jwtDecoder: JwtDecoder,
) {
    @GetMapping
    @Operation(summary = "Listar usuarios")
    fun findAll(): List<UserResponse> {
        return userService.findAll().map { it.toResponse() }
    }

    @GetMapping("/me")
    @Operation(summary = "Buscar perfil do usuario autenticado")
    fun findCurrentUser(
        servletRequest: HttpServletRequest,
    ): UserResponse =
        userService.findCurrentUser(authenticatedUid(servletRequest)).toResponse()

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
        servletRequest: HttpServletRequest,
        @Valid @RequestBody request: UpdateUserRequest,
    ): UserResponse =
        userService.updateCurrentUser(authenticatedUid(servletRequest), request).toResponse()

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
        servletRequest: HttpServletRequest,
    ) {
        userService.deleteCurrentUser(authenticatedUid(servletRequest))
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

    private fun authenticatedUid(request: HttpServletRequest): String {
        request.getAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE)
            ?.toString()
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }

        val token = BearerTokenExtractor.extract(request.getHeader(HttpHeaders.AUTHORIZATION))
            ?: throw UnauthorizedException("Authorization Bearer token is required")

        return jwtDecoder.decode(token).subject
            ?: throw UnauthorizedException("JWT subject is missing")
    }
}
