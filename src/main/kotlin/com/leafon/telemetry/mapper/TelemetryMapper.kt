package com.leafon.telemetry.mapper

import com.leafon.telemetry.dto.TelemetryResponse
import com.leafon.telemetry.entity.TelemetryReading

fun TelemetryReading.toResponse() = TelemetryResponse(
    soilHumidity = soilHumidityPercent,
    airHumidity = airHumidity,
    temperature = temperature,
    luminosityStatus = luminosity,
)
