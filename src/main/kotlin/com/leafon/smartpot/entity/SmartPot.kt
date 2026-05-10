package com.leafon.smartpot.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "smartpots")
class SmartPot(
    @Id
    @Column(nullable = false, updatable = false)
    var id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    var userId: UUID? = null,

    @Column(nullable = false)
    var plantName: String? = null,

    @Column(nullable = false)
    var humidityMin: Int? = null,

    @Column(unique = true)
    var deviceId: String? = null,

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
