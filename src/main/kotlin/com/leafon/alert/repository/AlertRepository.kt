package com.leafon.alert.repository

import com.leafon.alert.entity.Alert
import com.leafon.alert.enums.AlertStatus
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface AlertRepository : JpaRepository<Alert, UUID> {
    fun findAllBySmartPotUserIdOrderByCreatedAtDesc(userId: UUID): List<Alert>
    fun findAllBySmartPotUserIdAndStatusOrderByCreatedAtDesc(userId: UUID, status: AlertStatus): List<Alert>
    fun findByIdAndSmartPotUserId(id: UUID, userId: UUID): Alert?
}
