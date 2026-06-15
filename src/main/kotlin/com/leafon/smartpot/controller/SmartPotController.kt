package com.leafon.smartpot.controller

import com.leafon.auth.security.toAuthenticatedUserId
import com.leafon.common.config.SecurityConfig
import com.leafon.smartpot.dto.SmartPotCreateRequest
import com.leafon.smartpot.dto.SmartPotResponse
import com.leafon.smartpot.dto.SmartPotUpdateRequest
import com.leafon.smartpot.mapper.toResponse
import com.leafon.smartpot.service.SmartPotService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Smart Pots", description = "Gerenciamento dos vasos inteligentes do usuario autenticado.")
@SecurityRequirement(name = "bearerAuth")
class SmartPotController(
    private val smartPotService: SmartPotService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cadastrar smart pot")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Smart pot cadastrado"),
            ApiResponse(responseCode = "400", description = "Dados invalidos"),
            ApiResponse(responseCode = "409", description = "Device ID ja cadastrado"),
        ],
    )
    fun create(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @Valid @RequestBody request: SmartPotCreateRequest,
    ): SmartPotResponse =
        smartPotService.create(request, uid.toAuthenticatedUserId()).toResponse()

    @GetMapping
    @Operation(summary = "Listar smart pots")
    fun findAll(
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): List<SmartPotResponse> =
        smartPotService.findAll(uid.toAuthenticatedUserId()).map { it.toResponse() }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar smart pot por ID")
    fun findById(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ): SmartPotResponse =
        smartPotService.findById(id, uid.toAuthenticatedUserId()).toResponse()

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar smart pot")
    fun update(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
        @Valid @RequestBody request: SmartPotUpdateRequest,
    ): SmartPotResponse =
        smartPotService.update(id, request, uid.toAuthenticatedUserId()).toResponse()

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Excluir smart pot")
    fun delete(
        @PathVariable id: UUID,
        @RequestAttribute(SecurityConfig.AUTHENTICATED_UID_ATTRIBUTE) uid: String,
    ) {
        smartPotService.delete(id, uid.toAuthenticatedUserId())
    }
}
