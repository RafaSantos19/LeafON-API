package com.leafon.routine.repository

import com.leafon.routine.entity.Routine
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface RoutineRepository : JpaRepository<Routine, UUID> {
    fun findAllBySmartPotUserIdOrderByCreatedAtDesc(userId: UUID): List<Routine>
    fun findByIdAndSmartPotUserId(id: UUID, userId: UUID): Routine?
    fun findAllBySmartPotIdAndActiveTrue(smartPotId: UUID): List<Routine>
}
