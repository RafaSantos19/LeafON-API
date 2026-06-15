package com.leafon.telemetry.mqtt

import com.leafon.telemetry.entity.TelemetryReading
import com.leafon.telemetry.enums.Luminosity
import com.leafon.telemetry.service.TelemetryService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class TelemetryMqttMessageHandlerTest {
    @Mock
    private lateinit var telemetryService: TelemetryService

    @Test
    fun `persists valid MQTT telemetry payload`() {
        val smartPotId = UUID.randomUUID()
        val readAt = Instant.parse("2026-05-30T12:00:00Z")
        whenever(telemetryService.createFromDevice(eq(smartPotId), any(), eq(readAt)))
            .thenReturn(TelemetryReading(id = UUID.randomUUID(), readAt = readAt))
        val handler = TelemetryMqttMessageHandler(telemetryService)

        val result = handler.handle(
            """
            {
              "smartPotId": "$smartPotId",
              "soilHumidity": 92,
              "soilHumidityRaw": 352,
              "airHumidity": 68.4,
              "temperature": 21.4,
              "luminosityStatus": "CLARO",
              "luminosityDigital": 0,
              "readAt": "$readAt"
            }
            """.trimIndent(),
        )

        assertTrue(result)
        verify(telemetryService).createFromDevice(
            eq(smartPotId),
            org.mockito.kotlin.check {
                kotlin.test.assertEquals(92, it.soilHumidity)
                kotlin.test.assertEquals(68.4, it.airHumidity)
                kotlin.test.assertEquals(21.4, it.temperature)
                kotlin.test.assertEquals(Luminosity.CLARO, it.luminosityStatus)
            },
            eq(readAt),
        )
    }

    @Test
    fun `ignores invalid JSON`() {
        val handler = TelemetryMqttMessageHandler(telemetryService)

        assertFalse(handler.handle("{invalid"))
        verify(telemetryService, never()).createFromDevice(any(), any(), any())
    }

    @Test
    fun `ignores payload without smartPotId`() {
        val handler = TelemetryMqttMessageHandler(telemetryService)

        assertFalse(
            handler.handle(
                """
                {
                  "soilHumidity": 92,
                  "airHumidity": 68.4,
                  "temperature": 21.4,
                  "luminosityStatus": "CLARO"
                }
                """.trimIndent(),
            ),
        )
        verify(telemetryService, never()).createFromDevice(any(), any(), any())
    }

    @Test
    fun `keeps listener alive when persistence fails`() {
        val smartPotId = UUID.randomUUID()
        whenever(telemetryService.createFromDevice(eq(smartPotId), any(), any()))
            .thenThrow(IllegalStateException("database unavailable"))
        val handler = TelemetryMqttMessageHandler(telemetryService)

        val result = handler.handle(
            """
            {
              "smartPotId": "$smartPotId",
              "soilHumidity": 50,
              "airHumidity": 60.0,
              "temperature": 25.0,
              "luminosityStatus": "ESCURO"
            }
            """.trimIndent(),
        )

        assertFalse(result)
    }
}
