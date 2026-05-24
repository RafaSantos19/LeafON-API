package com.leafon.routine.service

import com.leafon.routine.dto.RoutineCreateRequest
import com.leafon.routine.dto.RoutineUpdateRequest
import com.leafon.routine.entity.Routine
import com.leafon.routine.exception.RoutineNotFoundException
import com.leafon.routine.repository.RoutineRepository
import com.leafon.smartpot.service.SmartPotService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class RoutineService(
    private val routineRepository: RoutineRepository,
    private val smartPotService: SmartPotService,
) {

    @Transactional
    fun create(
        request: RoutineCreateRequest,
        authenticatedUserId: UUID,
    ): Routine {
        val smartPot = smartPotService.findOwnedById(request.smartPotId, authenticatedUserId)

        return routineRepository.save(
            Routine(
                smartPot = smartPot,
                type = request.type,
                name = request.name.normalizedName(),
                scheduledTime = request.scheduledTime,
                daysOfWeek = request.daysOfWeek.normalizedDaysOfWeek(),
                durationSec = request.durationSec,
                active = request.active,
            ),
        )
    }

    fun findAll(authenticatedUserId: UUID): List<Routine> =
        routineRepository.findAllBySmartPotUserIdOrderByCreatedAtDesc(authenticatedUserId)

    fun findById(
        id: UUID,
        authenticatedUserId: UUID,
    ): Routine = findOwnedById(id, authenticatedUserId)

    @Transactional
    fun update(
        id: UUID,
        request: RoutineUpdateRequest,
        authenticatedUserId: UUID,
    ): Routine {
        val routine = findOwnedById(id, authenticatedUserId)

        routine.type = request.type
        routine.name = request.name.normalizedName()
        routine.scheduledTime = request.scheduledTime
        routine.daysOfWeek = request.daysOfWeek.normalizedDaysOfWeek()
        routine.durationSec = request.durationSec
        routine.active = request.active

        return routineRepository.save(routine)
    }

    @Transactional
    fun activate(
        id: UUID,
        authenticatedUserId: UUID,
    ): Routine {
        val routine = findOwnedById(id, authenticatedUserId)
        routine.active = true
        return routineRepository.save(routine)
    }

    @Transactional
    fun deactivate(
        id: UUID,
        authenticatedUserId: UUID,
    ): Routine {
        val routine = findOwnedById(id, authenticatedUserId)
        routine.active = false
        return routineRepository.save(routine)
    }

    @Transactional
    fun simulateExecution(
        id: UUID,
        authenticatedUserId: UUID,
    ): Routine {
        val routine = findOwnedById(id, authenticatedUserId)
        routine.lastExecutedAt = Instant.now()
        return routineRepository.save(routine)
    }

    @Transactional
    fun delete(
        id: UUID,
        authenticatedUserId: UUID,
    ) {
        routineRepository.delete(findOwnedById(id, authenticatedUserId))
    }

    private fun findOwnedById(
        id: UUID,
        authenticatedUserId: UUID,
    ): Routine =
        routineRepository.findByIdAndSmartPotUserId(id, authenticatedUserId)
            ?: throw RoutineNotFoundException(id)

    private fun String.normalizedName(): String =
        trim()

    private fun String.normalizedDaysOfWeek(): String =
        trim()
}
