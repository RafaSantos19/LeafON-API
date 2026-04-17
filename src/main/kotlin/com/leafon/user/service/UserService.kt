package com.leafon.user.service

import com.leafon.common.exception.ConflictException
import com.leafon.common.exception.NotFoundException
import com.leafon.user.dto.CreateUserRequest
import com.leafon.user.dto.UpdateUserRequest
import com.leafon.user.entity.User
import com.leafon.user.repository.UserRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun create(request: CreateUserRequest): User {
        if (userRepository.existsByEmail(request.email)) {
            throw ConflictException("Email already in use")
        }

        val user = User(
            email = request.email,
            name = request.name,
        )

        return userRepository.save(user)
    }

    fun findById(id: UUID): User {
        return userRepository.findById(id)
            .orElseThrow { NotFoundException("User with id $id not found") }
    }

    fun findByEmail(email: String): User {
        return userRepository.findByEmail(email)
            ?: throw NotFoundException("User with email $email not found")
    }

    fun findAll(): List<User> {
        return userRepository.findAll()
    }

    fun update(id: UUID, request: UpdateUserRequest): User {
        val existingUser = userRepository.findById(id)
            .orElseThrow { NotFoundException("User with id $id not found") }

        request.email?.let { newEmail ->
            if (newEmail != existingUser.email && userRepository.existsByEmail(newEmail)) {
                throw ConflictException("Email already in use")
            }
            existingUser.email = newEmail
        }

        request.name?.let { newName ->
            existingUser.name = newName
        }

        return userRepository.save(existingUser)
    }

    fun delete(id: UUID) {
        val existingUser = userRepository.findById(id)
            .orElseThrow { NotFoundException("User with id $id not found") }

        userRepository.delete(existingUser)
    }
}