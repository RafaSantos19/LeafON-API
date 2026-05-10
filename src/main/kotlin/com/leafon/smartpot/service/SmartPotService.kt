package com.leafon.smartpot.service

import com.leafon.common.exception.ConflictException
import com.leafon.smartpot.dto.SmartPotCreateRequest
import com.leafon.smartpot.dto.SmartPotUpdateRequest
import com.leafon.smartpot.entity.SmartPot
import com.leafon.smartpot.exception.SmartPotNotFoundException
import com.leafon.smartpot.repository.SmartPotRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class SmartPotService(
    private val smartPotRepository: SmartPotRepository,
) {

    @Transactional
    fun create(
        request: SmartPotCreateRequest,
        authenticatedUserId: UUID,
    ): SmartPot {
        val normalizedDeviceId = request.deviceId.normalizedDeviceId()
        ensureDeviceIdIsAvailable(normalizedDeviceId)

        return smartPotRepository.save(
            SmartPot(
                userId = authenticatedUserId,
                plantName = request.plantName.normalizedPlantName(),
                humidityMin = request.humidityMin,
                deviceId = normalizedDeviceId,
            ),
        )
    }

    fun findAll(authenticatedUserId: UUID): List<SmartPot> =
        smartPotRepository.findAllByUserId(authenticatedUserId)

    fun findById(
        id: UUID,
        authenticatedUserId: UUID,
    ): SmartPot = findOwnedById(id, authenticatedUserId)

    @Transactional
    fun update(
        id: UUID,
        request: SmartPotUpdateRequest,
        authenticatedUserId: UUID,
    ): SmartPot {
        val smartPot = findOwnedById(id, authenticatedUserId)
        val normalizedDeviceId = request.deviceId.normalizedDeviceId()

        ensureDeviceIdIsAvailable(
            deviceId = normalizedDeviceId,
            currentSmartPotId = smartPot.id ?: id,
        )

        smartPot.plantName = request.plantName.normalizedPlantName()
        smartPot.humidityMin = request.humidityMin
        smartPot.deviceId = normalizedDeviceId

        return smartPotRepository.save(smartPot)
    }

    @Transactional
    fun delete(
        id: UUID,
        authenticatedUserId: UUID,
    ) {
        smartPotRepository.delete(findOwnedById(id, authenticatedUserId))
    }

    fun findOwnedById(
        id: UUID,
        authenticatedUserId: UUID,
    ): SmartPot =
        smartPotRepository.findByIdAndUserId(id, authenticatedUserId)
            ?: throw SmartPotNotFoundException(id)

    private fun ensureDeviceIdIsAvailable(
        deviceId: String?,
        currentSmartPotId: UUID? = null,
    ) {
        if (deviceId == null) {
            return
        }

        val exists = if (currentSmartPotId == null) {
            smartPotRepository.existsByDeviceId(deviceId)
        } else {
            smartPotRepository.existsByDeviceIdAndIdNot(deviceId, currentSmartPotId)
        }

        if (exists) {
            throw ConflictException("Device id $deviceId is already in use")
        }
    }

    private fun String.normalizedPlantName(): String =
        trim()

    private fun String?.normalizedDeviceId(): String? =
        this?.trim()?.takeIf { it.isNotBlank() }
}
