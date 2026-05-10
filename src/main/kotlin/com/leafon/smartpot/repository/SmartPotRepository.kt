package com.leafon.smartpot.repository

import com.leafon.smartpot.entity.SmartPot
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface SmartPotRepository : JpaRepository<SmartPot, UUID> {
    fun findAllByUserId(userId: UUID): List<SmartPot>
    fun findByIdAndUserId(id: UUID, userId: UUID): SmartPot?
    fun existsByDeviceId(deviceId: String): Boolean
    fun existsByDeviceIdAndIdNot(deviceId: String, id: UUID): Boolean
}
