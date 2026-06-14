package com.leafon.alert.controller

import com.leafon.alert.dto.AlertResponse
import com.leafon.alert.mapper.toResponse
import com.leafon.alert.service.AlertService
import com.leafon.common.config.SecurityConfig
import com.leafon.common.exception.UnauthorizedException
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/alerts")
@Tag(name = "Alertas", description = "Consulta e atualizacao dos alertas dos smart pots.")
@SecurityRequirement(name = "bearerAuth")
class AlertController(
    private val alertService: AlertService,
) {

    @GetMapping
    @Operation(summary = "Listar alertas")
    fun findAll(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): List<AlertResponse> =
        alertService.findAll(authenticatedUserId(uid)).map { it.toResponse() }

    @GetMapping("/unread")
    @Operation(summary = "Listar alertas nao lidos")
    fun findUnread(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): List<AlertResponse> =
        alertService.findUnread(authenticatedUserId(uid)).map { it.toResponse() }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Marcar alerta como lido")
    fun markAsRead(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): AlertResponse =
        alertService.markAsRead(id, authenticatedUserId(uid)).toResponse()

    private fun authenticatedUserId(uid: String): UUID =
        runCatching { UUID.fromString(uid.trim()) }
            .getOrElse { throw UnauthorizedException("Authenticated UID is not a valid UUID") }
}
