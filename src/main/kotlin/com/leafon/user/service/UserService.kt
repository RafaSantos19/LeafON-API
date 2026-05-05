package com.leafon.user.service

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

    fun createAuthenticated(
        authenticatedUid: String,
        request: CreateUserRequest,
    ): User = createFromAuth(
        authenticatedUid = authenticatedUid,
        email = request.email,
        name = request.name,
        phone = request.phone,
    )

    @Transactional
    fun createFromAuth(
        authenticatedUid: String,
        email: String,
        name: String?,
        phone: String? = null,
    ): User {
        val authenticatedUserId = authenticatedUserId(authenticatedUid)
        val normalizedEmail = email.normalizedEmail()
        val normalizedName = name?.trim()?.takeIf { it.isNotBlank() }
        val normalizedPhone = phone?.normalizedPhone()

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

    @Transactional
    fun syncWithAuthIdentity(
        authenticatedUid: String,
        email: String?,
        name: String?,
        phone: String? = null,
    ): User {
        val authenticatedUserId = authenticatedUserId(authenticatedUid)
        val normalizedEmail = email?.normalizedEmail()
        val normalizedName = name?.trim()?.takeIf { it.isNotBlank() }
        val normalizedPhone = phone?.normalizedPhone()
        val existingUser = userRepository.findById(authenticatedUserId).orElse(null)

        normalizedEmail?.let { ensureEmailIsAvailable(it, authenticatedUserId) }

        if (existingUser == null && normalizedEmail == null) {
            throw ConflictException("Authenticated user email is required to create a local user")
        }

        val user = existingUser ?: User(
            id = authenticatedUserId,
            email = normalizedEmail,
            name = normalizedName,
            phone = normalizedPhone,
        )

        var changed = existingUser == null

        if (normalizedEmail != null && user.email != normalizedEmail) {
            ensureEmailIsAvailable(normalizedEmail, user.id)
            user.email = normalizedEmail
            changed = true
        }

        if (!normalizedName.isNullOrBlank() && user.name.isNullOrBlank()) {
            user.name = normalizedName
            changed = true
        }

        if (!normalizedPhone.isNullOrBlank() && user.phone != normalizedPhone) {
            user.phone = normalizedPhone
            changed = true
        }

        return if (changed) userRepository.save(user) else user
    }

    fun findById(id: UUID): User {
        return userRepository.findById(id)
            .orElseThrow { NotFoundException("User with id $id not found") }
    }

    fun findCurrentUser(authenticatedUid: String): User =
        findById(authenticatedUserId(authenticatedUid))

    fun findOwnedById(
        id: UUID,
        authenticatedUid: String,
    ): User =
        findById(id).also { ensureOwnership(it, authenticatedUid) }

    fun findByEmail(email: String): User {
        return userRepository.findByEmail(email.normalizedEmail())
            ?: throw NotFoundException("User with email $email not found")
    }

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

    private fun ensureEmailIsAvailable(
        email: String,
        currentUserId: UUID?,
    ) {
        val userWithEmail = userRepository.findByEmail(email) ?: return
        if (userWithEmail.id != currentUserId) {
            throw ConflictException("Email $email is already linked to another user")
        }
    }

    private fun authenticatedUserId(uid: String): UUID =
        runCatching { UUID.fromString(uid.trim()) }
            .getOrElse { throw UnauthorizedException("Authenticated UID is not a valid UUID") }

    private fun String.normalizedPhone(): String =
        trim()

    private fun String.normalizedEmail(): String =
        trim().lowercase()
}
