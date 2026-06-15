package com.leafon.routine.controller

import com.leafon.auth.security.toAuthenticatedUserId
import com.leafon.common.config.SecurityConfig
import com.leafon.routine.dto.RoutineCreateRequest
import com.leafon.routine.dto.RoutineResponse
import com.leafon.routine.dto.RoutineUpdateRequest
import com.leafon.routine.mapper.toResponse
import com.leafon.routine.service.RoutineService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Rotinas", description = "Gerenciamento das rotinas de irrigacao e iluminacao.")
@SecurityRequirement(name = "bearerAuth")
class RoutineController(
    private val routineService: RoutineService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar rotina")
    fun create(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @Valid @RequestBody request: RoutineCreateRequest,
    ): RoutineResponse =
        routineService.create(request, uid.toAuthenticatedUserId()).toResponse()

    @GetMapping
    @Operation(summary = "Listar rotinas")
    fun findAll(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): List<RoutineResponse> =
        routineService.findAll(uid.toAuthenticatedUserId()).map { it.toResponse() }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar rotina por ID")
    fun findById(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): RoutineResponse =
        routineService.findById(id, uid.toAuthenticatedUserId()).toResponse()

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar rotina")
    fun update(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @Valid @RequestBody request: RoutineUpdateRequest,
    ): RoutineResponse =
        routineService.update(id, request, uid.toAuthenticatedUserId()).toResponse()

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Ativar rotina")
    fun activate(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): RoutineResponse =
        routineService.activate(id, uid.toAuthenticatedUserId()).toResponse()

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Desativar rotina")
    fun deactivate(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): RoutineResponse =
        routineService.deactivate(id, uid.toAuthenticatedUserId()).toResponse()

    @PatchMapping("/{id}/simulate-execution")
    @Operation(summary = "Simular execucao da rotina")
    fun simulateExecution(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): RoutineResponse =
        routineService.simulateExecution(id, uid.toAuthenticatedUserId()).toResponse()

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir rotina")
    fun delete(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ) {
        routineService.delete(id, uid.toAuthenticatedUserId())
    }
}
