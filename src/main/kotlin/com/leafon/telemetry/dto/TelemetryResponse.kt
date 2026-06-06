package com.leafon.telemetry.dto

import com.leafon.telemetry.enums.Luminosity

data class TelemetryResponse(
    val soilHumidity: Int?,
    val airHumidity: Double?,
    val temperature: Double?,
    val luminosityStatus: Luminosity?,
)
