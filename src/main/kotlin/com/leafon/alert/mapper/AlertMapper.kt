package com.leafon.alert.mapper

import com.leafon.alert.dto.AlertResponse
import com.leafon.alert.entity.Alert

fun Alert.toResponse() = AlertResponse(
    id = id,
    smartPotId = smartPot?.id,
    telemetryReadingId = telemetryReading?.id,
    type = type,
    message = message,
    status = status,
    createdAt = createdAt,
    readAt = readAt,
)
