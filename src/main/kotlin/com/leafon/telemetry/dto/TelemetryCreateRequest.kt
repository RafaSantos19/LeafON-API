package com.leafon.telemetry.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.leafon.telemetry.enums.Luminosity
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

@JsonIgnoreProperties(ignoreUnknown = true)
data class TelemetryCreateRequest(
    @field:Min(0)
    @field:Max(100)
    val soilHumidity: Int,

    @field:Min(0)
    @field:Max(100)
    val airHumidity: Double,

    val temperature: Double,

    @field:NotNull
    val luminosityStatus: Luminosity,
)
