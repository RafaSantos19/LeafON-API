package com.leafon.smartpot.dto

import java.time.Instant
import java.util.UUID

data class SmartPotResponse(
    val id: UUID?,
    val plantName: String?,
    val humidityMin: Int?,
    val deviceId: String?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
)
