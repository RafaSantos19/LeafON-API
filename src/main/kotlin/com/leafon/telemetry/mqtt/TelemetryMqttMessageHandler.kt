package com.leafon.telemetry.mqtt

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.leafon.telemetry.dto.TelemetryCreateRequest
import com.leafon.telemetry.service.TelemetryService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TelemetryMqttMessageHandler(
    private val telemetryService: TelemetryService,
) {
    private val logger = LoggerFactory.getLogger(TelemetryMqttMessageHandler::class.java)
    private val objectMapper = jacksonObjectMapper().findAndRegisterModules()

    fun handle(payloadJson: String): Boolean {
        val payload = try {
            objectMapper.readValue<TelemetryMqttPayload>(payloadJson)
        } catch (ex: Exception) {
            logger.warn("MQTT telemetry message ignored because the JSON payload is invalid: {}", ex.message)
            return false
        }

        val smartPotId = payload.smartPotId
        if (smartPotId == null) {
            logger.warn("MQTT telemetry message ignored because smartPotId is missing")
            return false
        }

        val request = payload.toCreateRequest()
        if (request == null) {
            logger.warn(
                "MQTT telemetry message ignored because required sensor fields are missing for smartPotId={}",
                smartPotId,
            )
            return false
        }

        return try {
            val reading = telemetryService.createFromDevice(
                smartPotId = smartPotId,
                request = request,
                readAt = payload.readAt,
            )
            logger.info(
                "MQTT telemetry persisted smartPotId={} readingId={} topicReadAt={}",
                smartPotId,
                reading.id,
                reading.readAt,
            )
            true
        } catch (ex: Exception) {
            logger.warn(
                "MQTT telemetry message failed for smartPotId={}: {}",
                smartPotId,
                ex.message ?: ex::class.simpleName,
            )
            false
        }
    }

    private fun TelemetryMqttPayload.toCreateRequest(): TelemetryCreateRequest? {
        val soilHumidity = soilHumidity ?: return null
        val airHumidity = airHumidity ?: return null
        val temperature = temperature ?: return null
        val luminosityStatus = luminosityStatus ?: return null

        return TelemetryCreateRequest(
            soilHumidity = soilHumidity,
            airHumidity = airHumidity,
            temperature = temperature,
            luminosityStatus = luminosityStatus,
        )
    }
}
