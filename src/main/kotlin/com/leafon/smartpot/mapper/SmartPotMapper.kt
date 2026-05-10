package com.leafon.smartpot.mapper

import com.leafon.smartpot.dto.SmartPotResponse
import com.leafon.smartpot.entity.SmartPot

fun SmartPot.toResponse() = SmartPotResponse(
    id = id,
    plantName = plantName,
    humidityMin = humidityMin,
    deviceId = deviceId,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
