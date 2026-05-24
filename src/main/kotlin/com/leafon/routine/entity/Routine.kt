package com.leafon.routine.entity

import com.leafon.routine.enums.RoutineType
import com.leafon.smartpot.entity.SmartPot
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.Instant
import java.time.LocalTime
import java.util.UUID

@Entity
@Table(name = "routines")
class Routine(
    @Id
    @Column(nullable = false, updatable = false)
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "smart_pot_id", nullable = false, updatable = false)
    var smartPot: SmartPot? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: RoutineType? = null,

    @Column(nullable = false)
    var name: String? = null,

    @Column(nullable = false)
    var scheduledTime: LocalTime? = null,

    @Column(nullable = false)
    var daysOfWeek: String? = null,

    @Column(nullable = false)
    var durationSec: Int? = null,

    @Column(nullable = false)
    var active: Boolean? = null,

    @Column
    var lastExecutedAt: Instant? = null,

    @Column(nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @Column(nullable = false)
    var updatedAt: Instant? = null,
) {
    @PrePersist
    fun onCreate() {
        val now = Instant.now()

        if (id == null) {
            id = UUID.randomUUID()
        }

        if (createdAt == null) {
            createdAt = now
        }

        updatedAt = now
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = Instant.now()
    }
}
