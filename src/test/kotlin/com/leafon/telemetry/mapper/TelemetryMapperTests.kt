package com.leafon.telemetry.mapper

import com.leafon.telemetry.entity.TelemetryReading
import com.leafon.telemetry.enums.Luminosity
import kotlin.test.Test
import kotlin.test.assertEquals

class TelemetryMapperTests {

    @Test
    fun `maps telemetry reading to sensor response contract`() {
        val reading = TelemetryReading(
            soilHumidityPercent = 92,
            airHumidity = 68.4,
            temperature = 21.4,
            luminosity = Luminosity.CLARO,
        )

        assertEquals(
            expected = mapOf(
                "soilHumidity" to 92,
                "airHumidity" to 68.4,
                "temperature" to 21.4,
                "luminosityStatus" to Luminosity.CLARO,
            ),
            actual = reading.toResponse().let {
                mapOf(
                    "soilHumidity" to it.soilHumidity,
                    "airHumidity" to it.airHumidity,
                    "temperature" to it.temperature,
                    "luminosityStatus" to it.luminosityStatus,
                )
            },
        )
    }
}
