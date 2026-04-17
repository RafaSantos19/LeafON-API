package com.leafon.common.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(ex: NotFoundException): Map<String, Any> =
        mapOf(
            "timestamp" to Instant.now().toString(),
            "status" to 404,
            "error" to "Not Found",
            "message" to ex.message.orEmpty(),
        )

    @ExceptionHandler(ConflictException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleConflict(ex: ConflictException): Map<String, Any> =
        mapOf(
            "timestamp" to Instant.now().toString(),
            "status" to 409,
            "error" to "Conflict",
            "message" to ex.message.orEmpty(),
        )

    @ExceptionHandler(Exception::class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleGeneric(ex: Exception): Map<String, Any> =
        mapOf(
            "timestamp" to Instant.now().toString(),
            "status" to 500,
            "error" to "Internal Server Error",
            "message" to "Unexpected error occurred",
        )
}