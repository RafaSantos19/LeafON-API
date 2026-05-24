package com.leafon.alert.service

import com.leafon.alert.entity.Alert
import com.leafon.alert.enums.AlertStatus
import com.leafon.alert.enums.AlertType
import com.leafon.alert.exception.AlertNotFoundException
import com.leafon.alert.repository.AlertRepository
import com.leafon.smartpot.entity.SmartPot
import com.leafon.telemetry.entity.TelemetryReading
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class AlertService(
    private val alertRepository: AlertRepository,
) {

    @Transactional
    fun createLowSoilHumidityAlert(
        smartPot: SmartPot,
        telemetryReading: TelemetryReading,
    ): Alert =
        alertRepository.save(
            Alert(
                smartPot = smartPot,
                telemetryReading = telemetryReading,
                type = AlertType.LOW_SOIL_HUMIDITY,
                message = LOW_SOIL_HUMIDITY_MESSAGE,
                status = AlertStatus.PENDING,
            ),
        )

    fun findAll(authenticatedUserId: UUID): List<Alert> =
        alertRepository.findAllBySmartPotUserIdOrderByCreatedAtDesc(authenticatedUserId)

    fun findUnread(authenticatedUserId: UUID): List<Alert> =
        alertRepository.findAllBySmartPotUserIdAndStatusOrderByCreatedAtDesc(
            authenticatedUserId,
            AlertStatus.PENDING,
        )

    @Transactional
    fun markAsRead(
        alertId: UUID,
        authenticatedUserId: UUID,
    ): Alert {
        val alert = alertRepository.findByIdAndSmartPotUserId(alertId, authenticatedUserId)
            ?: throw AlertNotFoundException(alertId)

        if (alert.status != AlertStatus.READ) {
            alert.status = AlertStatus.READ
            alert.readAt = Instant.now()
        }

        return alertRepository.save(alert)
    }

    companion object {
        private const val LOW_SOIL_HUMIDITY_MESSAGE =
            "Umidade do solo abaixo do limite configurado para este vaso."
    }
}
