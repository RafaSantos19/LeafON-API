package com.leafon.routine.dto

import com.leafon.routine.enums.RoutineType
import java.time.Instant
import java.time.LocalTime
import java.util.UUID

data class RoutineResponse(
    val id: UUID?,
    val smartPotId: UUID?,
    val type: RoutineType?,
    val name: String?,
    val scheduledTime: LocalTime?,
    val daysOfWeek: String?,
    val durationSec: Int?,
    val active: Boolean?,
    val lastExecutedAt: Instant?,
    val createdAt: Instant?,
    val updatedAt: Instant?,
)
