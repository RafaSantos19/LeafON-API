package com.leafon.telemetry.controller

import com.leafon.common.config.SecurityConfig
import com.leafon.common.exception.UnauthorizedException
import com.leafon.telemetry.dto.TelemetryCreateRequest
import com.leafon.telemetry.dto.TelemetryResponse
import com.leafon.telemetry.mapper.toResponse
import com.leafon.telemetry.service.TelemetryService
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
class TelemetryController(
    private val telemetryService: TelemetryService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @Valid @RequestBody request: TelemetryCreateRequest,
    ): TelemetryResponse =
        telemetryService.create(request, authenticatedUserId(uid)).toResponse()

    //Params http://localhost:8080/telemetry?smartPotId=273f9192-d2c1-467d-9855-3f0e502e9f42
    @GetMapping
    fun findAll(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @RequestParam smartPotId: UUID,
    ): List<TelemetryResponse> =
        telemetryService.findAll(smartPotId, authenticatedUserId(uid)).map { it.toResponse() }

    @GetMapping("/latest")
    fun findLatest(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @RequestParam smartPotId: UUID,
    ): TelemetryResponse =
        telemetryService.findLatest(smartPotId, authenticatedUserId(uid)).toResponse()

    private fun authenticatedUserId(uid: String): UUID =
        runCatching { UUID.fromString(uid.trim()) }
            .getOrElse { throw UnauthorizedException("Authenticated UID is not a valid UUID") }
}
