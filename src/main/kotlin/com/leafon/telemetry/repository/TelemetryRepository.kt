package com.leafon.telemetry.repository

import com.leafon.telemetry.entity.TelemetryReading
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TelemetryRepository : JpaRepository<TelemetryReading, UUID> {
    fun findAllBySmartPotIdOrderByReadAtDesc(smartPotId: UUID): List<TelemetryReading>
    fun findTopBySmartPotIdOrderByReadAtDesc(smartPotId: UUID): TelemetryReading?
}
