package com.leafon.telemetry.controller

import com.leafon.common.config.SecurityConfig
import com.leafon.common.exception.UnauthorizedException
import com.leafon.telemetry.dto.TelemetryCreateRequest
import com.leafon.telemetry.dto.TelemetryResponse
import com.leafon.telemetry.mapper.toResponse
import com.leafon.telemetry.service.TelemetryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/telemetry")
@Tag(name = "Telemetria", description = "Registro e consulta de leituras dos smart pots.")
@SecurityRequirement(name = "bearerAuth")
class TelemetryController(
    private val telemetryService: TelemetryService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar leitura de telemetria")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Leitura registrada"),
            ApiResponse(responseCode = "400", description = "Leitura invalida"),
            ApiResponse(responseCode = "403", description = "Smart pot nao pertence ao usuario"),
        ],
    )
    fun create(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @RequestParam smartPotId: UUID,
        @Valid @RequestBody request: TelemetryCreateRequest,
    ): TelemetryResponse =
        telemetryService.create(smartPotId, request, authenticatedUserId(uid)).toResponse()

    @GetMapping
    @Operation(summary = "Listar leituras de um smart pot")
    fun findAll(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @RequestParam smartPotId: UUID,
    ): List<TelemetryResponse> =
        telemetryService.findAll(smartPotId, authenticatedUserId(uid)).map { it.toResponse() }

    @GetMapping("/latest")
    @Operation(summary = "Buscar ultima leitura de um smart pot")
    fun findLatest(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @RequestParam smartPotId: UUID,
    ): TelemetryResponse =
        telemetryService.findLatest(smartPotId, authenticatedUserId(uid)).toResponse()

    private fun authenticatedUserId(uid: String): UUID =
        runCatching { UUID.fromString(uid.trim()) }
            .getOrElse { throw UnauthorizedException("Authenticated UID is not a valid UUID") }
}
