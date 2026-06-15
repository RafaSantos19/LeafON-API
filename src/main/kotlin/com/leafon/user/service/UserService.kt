package com.leafon.user.service

import com.leafon.auth.security.toAuthenticatedUserId
import com.leafon.common.exception.ConflictException
import com.leafon.common.exception.ForbiddenException
import com.leafon.common.exception.NotFoundException
import com.leafon.common.exception.UnauthorizedException
import com.leafon.user.dto.CreateUserRequest
import com.leafon.user.dto.UpdateUserRequest
import com.leafon.user.entity.User
import com.leafon.user.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository
) {
    @Transactional
    fun createAuthenticated(
        authenticatedUid: String,
        request: CreateUserRequest,
    ): User {
        val authenticatedUserId = authenticatedUserId(authenticatedUid)
        val normalizedEmail = request.email.normalizedEmail()
        val normalizedName = request.name?.trim()?.takeIf { it.isNotBlank() }
        val normalizedPhone = request.phone?.normalizedPhone()

        if (userRepository.existsById(authenticatedUserId)) {
            throw ConflictException("Authenticated user is already linked to a local user")
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw ConflictException("Email already in use")
        }

        return userRepository.save(
            User(
                id = authenticatedUserId,
                email = normalizedEmail,
                name = normalizedName,
                phone = normalizedPhone,
            ),
        )
    }

    fun findById(id: UUID): User {
        return userRepository.findById(id)
            .orElseThrow { NotFoundException("User with id $id not found") }
    }

    fun findCurrentUser(authenticatedUid: String): User =
        findByAuthenticatedIdentity(authenticatedUid)

    fun findOwnedById(
        id: UUID,
        authenticatedUid: String,
    ): User =
        findById(id).also { ensureOwnership(it, authenticatedUid) }

    fun findAll(): List<User> {
        return userRepository.findAll()
    }

    fun updateCurrentUser(
        authenticatedUid: String,
        request: UpdateUserRequest,
    ): User =
        update(findCurrentUser(authenticatedUid), request)

    fun updateOwnedUser(
        id: UUID,
        authenticatedUid: String,
        request: UpdateUserRequest,
    ): User =
        update(findOwnedById(id, authenticatedUid), request)

    fun deleteCurrentUser(authenticatedUid: String) {
        userRepository.delete(findCurrentUser(authenticatedUid))
    }

    fun deleteOwnedUser(
        id: UUID,
        authenticatedUid: String,
    ) {
        userRepository.delete(findOwnedById(id, authenticatedUid))
    }

    private fun update(
        existingUser: User,
        request: UpdateUserRequest,
    ): User {
        request.email?.let { newEmail ->
            val normalizedEmail = newEmail.normalizedEmail()
            if (normalizedEmail != existingUser.email && userRepository.existsByEmail(normalizedEmail)) {
                throw ConflictException("Email already in use")
            }
            existingUser.email = normalizedEmail
        }

        request.name?.let { newName ->
            existingUser.name = newName.trim()
        }

        request.phone?.let { newPhone ->
            existingUser.phone = newPhone.normalizedPhone()
        }

        return userRepository.save(existingUser)
    }

    private fun ensureOwnership(
        user: User,
        authenticatedUid: String,
    ) {
        if (user.id != authenticatedUserId(authenticatedUid)) {
            throw ForbiddenException("User does not belong to the authenticated account")
        }
    }

    private fun authenticatedUserId(uid: String): UUID =
        uid.toAuthenticatedUserId()

    private fun findByAuthenticatedIdentity(identity: String): User {
        runCatching { return findById(identity.toAuthenticatedUserId()) }

        val email = identity.normalizedEmail()
        return userRepository.findByEmail(email)
            ?: throw UnauthorizedException("Authenticated identity is not linked to a local user")
    }

    private fun String.normalizedPhone(): String =
        trim()

    private fun String.normalizedEmail(): String =
        trim().lowercase()
}
