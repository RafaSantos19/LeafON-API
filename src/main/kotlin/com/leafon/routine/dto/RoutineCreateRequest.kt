package com.leafon.routine.dto

import com.leafon.routine.enums.RoutineType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import java.time.LocalTime
import java.util.UUID

data class RoutineCreateRequest(
    @field:NotNull
    val smartPotId: UUID,

    @field:NotNull
    val type: RoutineType,

    @field:NotBlank
    val name: String,

    @field:NotNull
    val scheduledTime: LocalTime,

    @field:NotBlank
    val daysOfWeek: String,

    @field:Positive
    val durationSec: Int,

    @field:NotNull
    val active: Boolean,
)
