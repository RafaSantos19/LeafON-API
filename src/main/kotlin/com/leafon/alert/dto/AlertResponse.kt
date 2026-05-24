package com.leafon.alert.dto

import com.leafon.alert.enums.AlertStatus
import com.leafon.alert.enums.AlertType
import java.time.Instant
import java.util.UUID

data class AlertResponse(
    val id: UUID?,
    val smartPotId: UUID?,
    val telemetryReadingId: UUID?,
    val type: AlertType?,
    val message: String?,
    val status: AlertStatus?,
    val createdAt: Instant?,
    val readAt: Instant?,
)
