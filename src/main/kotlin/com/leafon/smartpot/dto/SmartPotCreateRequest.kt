package com.leafon.smartpot.dto

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class SmartPotCreateRequest(
    @field:NotBlank
    val plantName: String,

    @field:Min(0)
    @field:Max(100)
    val humidityMin: Int,

    val deviceId: String?,
)
