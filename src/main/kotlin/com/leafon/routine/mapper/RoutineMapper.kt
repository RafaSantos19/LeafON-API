package com.leafon.routine.mapper

import com.leafon.routine.dto.RoutineResponse
import com.leafon.routine.entity.Routine

fun Routine.toResponse() = RoutineResponse(
    id = id,
    smartPotId = smartPot?.id,
    type = type,
    name = name,
    scheduledTime = scheduledTime,
    daysOfWeek = daysOfWeek,
    durationSec = durationSec,
    active = active,
    lastExecutedAt = lastExecutedAt,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
