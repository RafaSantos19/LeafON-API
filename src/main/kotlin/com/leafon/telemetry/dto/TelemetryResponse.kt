package com.leafon.telemetry.dto

import java.time.Instant
import java.util.UUID

data class TelemetryResponse(
    val id: UUID?,
    val smartPotId: UUID?,
    val soilHumidity: Int?,
    val temperature: Double?,
    val luminosity: Double?,
    val readAt: Instant?,
    val createdAt: Instant?,
)
