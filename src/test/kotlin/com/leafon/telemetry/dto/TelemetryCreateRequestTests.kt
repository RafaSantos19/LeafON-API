package com.leafon.telemetry.dto

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.leafon.telemetry.enums.Luminosity
import kotlin.test.Test
import kotlin.test.assertEquals

class TelemetryCreateRequestTests {

    @Test
    fun `accepts sensor payload and ignores unused fields`() {
        val payload = """
            {
              "soilHumidity": 92,
              "soilHumidityRaw": 352,
              "airHumidity": 68.4,
              "temperature": 21.4,
              "luminosityStatus": "CLARO",
              "luminosityDigital": 0,
              "readAt": "2026-05-30T12:00:00Z"
            }
        """.trimIndent()

        val request = jacksonObjectMapper().readValue<TelemetryCreateRequest>(payload)

        assertEquals(92, request.soilHumidity)
        assertEquals(68.4, request.airHumidity)
        assertEquals(21.4, request.temperature)
        assertEquals(Luminosity.CLARO, request.luminosityStatus)
    }
}
