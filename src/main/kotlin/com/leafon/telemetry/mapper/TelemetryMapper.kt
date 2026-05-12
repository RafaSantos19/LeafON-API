package com.leafon.telemetry.mapper

import com.leafon.telemetry.dto.TelemetryResponse
import com.leafon.telemetry.entity.TelemetryReading

fun TelemetryReading.toResponse() = TelemetryResponse(
    id = id,
    smartPotId = smartPot?.id,
    soilHumidity = soilHumidity,
    temperature = temperature,
    luminosity = luminosity,
    readAt = readAt,
    createdAt = createdAt,
)
