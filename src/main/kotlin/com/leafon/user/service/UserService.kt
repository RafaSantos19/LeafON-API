package com.leafon.user.service

import com.leafon.user.dto.CreateUserRequest
import com.leafon.user.dto.UpdateUserRequest
import com.leafon.user.entity.User
import com.leafon.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService (
    private val userRepository: UserRepository
) {
    fun create(request: CreateUserRequest): User {
        val user = User(
            email = request.email,
            name = request.name,
        )

        return userRepository.save(user)
    }

    fun findById(id: UUID): User? {
        return userRepository.findById(id).orElse(null)
    }

    fun findBySupabaseUserId(userId: UUID): User? {
        return userRepository.findBySupabaseUserId(userId)
    }

    fun findByEmail(email: String): User? {
        return userRepository.findByEmail(email)
    }

    fun update(id: UUID, request: UpdateUserRequest): User? {
        val existingUser = userRepository.findById(id).orElse(null) ?: return null

        if (request.name != null) {
            existingUser.name = request.name
        }

        if (request.email != null) {
            existingUser.email = request.email
        }

        return userRepository.save(existingUser)
    }

    fun delete(id: UUID) {
        userRepository.deleteById(id)
    }
}
