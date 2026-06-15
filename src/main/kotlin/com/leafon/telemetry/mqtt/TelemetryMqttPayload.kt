package com.leafon.telemetry.mqtt

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.leafon.telemetry.enums.Luminosity
import java.time.Instant
import java.util.UUID

@JsonIgnoreProperties(ignoreUnknown = true)
data class TelemetryMqttPayload(
    val smartPotId: UUID? = null,
    val soilHumidity: Int? = null,
    val soilHumidityRaw: Int? = null,
    val airHumidity: Double? = null,
    val temperature: Double? = null,
    val luminosityStatus: Luminosity? = null,
    val luminosityDigital: Int? = null,
    val readAt: Instant? = null,
)
