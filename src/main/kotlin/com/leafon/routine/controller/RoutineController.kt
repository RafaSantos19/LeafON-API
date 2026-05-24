package com.leafon.routine.controller

import com.leafon.common.config.SecurityConfig
import com.leafon.common.exception.UnauthorizedException
import com.leafon.routine.dto.RoutineCreateRequest
import com.leafon.routine.dto.RoutineResponse
import com.leafon.routine.dto.RoutineUpdateRequest
import com.leafon.routine.mapper.toResponse
import com.leafon.routine.service.RoutineService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
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
@RequestMapping("/routines")
class RoutineController(
    private val routineService: RoutineService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @Valid @RequestBody request: RoutineCreateRequest,
    ): RoutineResponse =
        routineService.create(request, authenticatedUserId(uid)).toResponse()

    @GetMapping
    fun findAll(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): List<RoutineResponse> =
        routineService.findAll(authenticatedUserId(uid)).map { it.toResponse() }

    @GetMapping("/{id}")
    fun findById(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): RoutineResponse =
        routineService.findById(id, authenticatedUserId(uid)).toResponse()

    @PutMapping("/{id}")
    fun update(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @Valid @RequestBody request: RoutineUpdateRequest,
    ): RoutineResponse =
        routineService.update(id, request, authenticatedUserId(uid)).toResponse()

    @PatchMapping("/{id}/activate")
    fun activate(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): RoutineResponse =
        routineService.activate(id, authenticatedUserId(uid)).toResponse()

    @PatchMapping("/{id}/deactivate")
    fun deactivate(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): RoutineResponse =
        routineService.deactivate(id, authenticatedUserId(uid)).toResponse()

    @PatchMapping("/{id}/simulate-execution")
    fun simulateExecution(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): RoutineResponse =
        routineService.simulateExecution(id, authenticatedUserId(uid)).toResponse()

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ) {
        routineService.delete(id, authenticatedUserId(uid))
    }

    private fun authenticatedUserId(uid: String): UUID =
        runCatching { UUID.fromString(uid.trim()) }
            .getOrElse { throw UnauthorizedException("Authenticated UID is not a valid UUID") }
}
