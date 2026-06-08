package com.leafon.telemetry.service

import com.leafon.alert.service.AlertService
import com.leafon.common.exception.ForbiddenException
import com.leafon.common.exception.NotFoundException
import com.leafon.smartpot.entity.SmartPot
import com.leafon.smartpot.exception.SmartPotNotFoundException
import com.leafon.smartpot.repository.SmartPotRepository
import com.leafon.telemetry.dto.TelemetryCreateRequest
import com.leafon.telemetry.entity.TelemetryReading
import com.leafon.telemetry.repository.TelemetryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class TelemetryService(
    private val telemetryRepository: TelemetryRepository,
    private val smartPotRepository: SmartPotRepository,
    private val alertService: AlertService,
) {

    @Transactional
    fun create(
        smartPotId: UUID,
        request: TelemetryCreateRequest,
        authenticatedUserId: UUID,
    ): TelemetryReading {
        val smartPot = findAccessibleSmartPot(smartPotId, authenticatedUserId)

        val telemetryReading = telemetryRepository.save(
            TelemetryReading(
                smartPot = smartPot,
                soilHumidityPercent = request.soilHumidity,
                airHumidity = request.airHumidity,
                temperature = request.temperature,
                luminosity = request.luminosityStatus,
                readAt = Instant.now(),
            ),
        )

        maybeCreateAlerts(smartPot, telemetryReading)

        return telemetryReading
    }

    fun findAll(
        smartPotId: UUID,
        authenticatedUserId: UUID,
    ): List<TelemetryReading> {
        findAccessibleSmartPot(smartPotId, authenticatedUserId)
        return telemetryRepository.findAllBySmartPotIdOrderByReadAtDesc(smartPotId)
    }

    fun findLatest(
        smartPotId: UUID,
        authenticatedUserId: UUID,
    ): TelemetryReading {
        findAccessibleSmartPot(smartPotId, authenticatedUserId)

        return telemetryRepository.findTopBySmartPotIdOrderByReadAtDesc(smartPotId)
            ?: throw NotFoundException("Telemetry reading not found for SmartPot with id $smartPotId")
    }

    private fun findAccessibleSmartPot(
        smartPotId: UUID,
        authenticatedUserId: UUID,
    ): SmartPot {
        val smartPot = smartPotRepository.findById(smartPotId)
            .orElseThrow { SmartPotNotFoundException(smartPotId) }

        if (smartPot.userId != authenticatedUserId) {
            throw ForbiddenException("SmartPot does not belong to the authenticated account")
        }

        return smartPot
    }

    private fun maybeCreateAlerts(
        smartPot: SmartPot,
        telemetryReading: TelemetryReading,
    ) {
        maybeCreateLowSoilHumidityAlert(smartPot, telemetryReading)
        maybeCreateLowAirHumidityAlert(smartPot, telemetryReading)
        maybeCreateHighTemperatureAlert(smartPot, telemetryReading)
    }

    private fun maybeCreateLowSoilHumidityAlert(
        smartPot: SmartPot,
        telemetryReading: TelemetryReading,
    ) {
        val humidityMin = smartPot.humidityMin ?: return
        val soilHumidity = telemetryReading.soilHumidityPercent ?: return

        if (soilHumidity < humidityMin) {
            alertService.createLowSoilHumidityAlert(smartPot, telemetryReading)
        }
    }

    private fun maybeCreateLowAirHumidityAlert(
        smartPot: SmartPot,
        telemetryReading: TelemetryReading,
    ) {
        val airHumidity = telemetryReading.airHumidity ?: return

        if (airHumidity < MIN_AIR_HUMIDITY_PERCENT) {
            alertService.createLowAirHumidityAlert(smartPot, telemetryReading)
        }
    }

    private fun maybeCreateHighTemperatureAlert(
        smartPot: SmartPot,
        telemetryReading: TelemetryReading,
    ) {
        val temperature = telemetryReading.temperature ?: return

        if (temperature > MAX_TEMPERATURE_CELSIUS) {
            alertService.createHighTemperatureAlert(smartPot, telemetryReading)
        }
    }

    companion object {
        private const val MIN_AIR_HUMIDITY_PERCENT = 40.0
        private const val MAX_TEMPERATURE_CELSIUS = 35.0
    }
}
