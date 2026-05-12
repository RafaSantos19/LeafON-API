package com.leafon.telemetry.entity

import com.leafon.smartpot.entity.SmartPot
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "telemetry_readings")
class TelemetryReading(
    @Id
    @Column(nullable = false, updatable = false)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "smart_pot_id", nullable = false, updatable = false)
    var smartPot: SmartPot? = null,

    @Column(nullable = false, updatable = false)
    var soilHumidity: Int? = null,

    @Column(nullable = false, updatable = false)
    var temperature: Double? = null,

    @Column(nullable = false, updatable = false)
    var luminosity: Double? = null,

    @Column(nullable = false, updatable = false)
    var readAt: Instant? = null,

    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,
) {
    @PrePersist
    fun onCreate() {
        if (id == null) {
            id = UUID.randomUUID()
        }

        if (createdAt == null) {
            createdAt = Instant.now()
        }
    }
}
