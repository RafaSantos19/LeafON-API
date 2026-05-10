package com.leafon.smartpot.controller

import com.leafon.common.config.SecurityConfig
import com.leafon.common.exception.UnauthorizedException
import com.leafon.smartpot.dto.SmartPotCreateRequest
import com.leafon.smartpot.dto.SmartPotResponse
import com.leafon.smartpot.dto.SmartPotUpdateRequest
import com.leafon.smartpot.mapper.toResponse
import com.leafon.smartpot.service.SmartPotService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/smart-pots")
class SmartPotController(
    private val smartPotService: SmartPotService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @Valid @RequestBody request: SmartPotCreateRequest,
    ): SmartPotResponse =
        smartPotService.create(request, authenticatedUserId(uid)).toResponse()

    @GetMapping
    fun findAll(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): List<SmartPotResponse> =
        smartPotService.findAll(authenticatedUserId(uid)).map { it.toResponse() }

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): SmartPotResponse =
        smartPotService.findById(id, authenticatedUserId(uid)).toResponse()

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @Valid @RequestBody request: SmartPotUpdateRequest,
    ): SmartPotResponse =
        smartPotService.update(id, request, authenticatedUserId(uid)).toResponse()

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ) {
        smartPotService.delete(id, authenticatedUserId(uid))
    }

    private fun authenticatedUserId(uid: String): UUID =
        runCatching { UUID.fromString(uid.trim()) }
            .getOrElse { throw UnauthorizedException("Authenticated UID is not a valid UUID") }
}
