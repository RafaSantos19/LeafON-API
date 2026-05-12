package com.leafon.telemetry.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.util.UUID

data class TelemetryCreateRequest(
    // TODO: smartPotId no body e temporario; substituir quando a ingestao deixar de depender da API autenticada pelo cliente.
    @field:NotNull
    val smartPotId: UUID,

    @field:Min(0)
    @field:Max(100)
    val soilHumidity: Int,

    val temperature: Double,

    val luminosity: Double,

    @field:NotNull
    val readAt: Instant,
)
