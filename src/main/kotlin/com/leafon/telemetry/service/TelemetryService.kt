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
import java.util.UUID

@Service
class TelemetryService(
    private val telemetryRepository: TelemetryRepository,
    private val smartPotRepository: SmartPotRepository,
    private val alertService: AlertService,
) {

    @Transactional
    fun create(
        request: TelemetryCreateRequest,
        authenticatedUserId: UUID,
    ): TelemetryReading {
        val smartPot = findAccessibleSmartPot(request.smartPotId, authenticatedUserId)

        val telemetryReading = telemetryRepository.save(
            TelemetryReading(
                smartPot = smartPot,
                soilHumidity = request.soilHumidity,
                temperature = request.temperature,
                luminosity = request.luminosity,
                readAt = request.readAt,
            ),
        )

        maybeCreateLowSoilHumidityAlert(smartPot, telemetryReading)

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

    private fun maybeCreateLowSoilHumidityAlert(
        smartPot: SmartPot,
        telemetryReading: TelemetryReading,
    ) {
        val humidityMin = smartPot.humidityMin ?: return
        val soilHumidity = telemetryReading.soilHumidity ?: return

        if (soilHumidity < humidityMin) {
            alertService.createLowSoilHumidityAlert(smartPot, telemetryReading)
        }
    }
}
