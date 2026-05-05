package com.leafon.common.exception

import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ExternalServiceException::class)
    @ResponseStatus(HttpStatus.BAD_GATEWAY)
    fun handleExternalService(ex: ExternalServiceException): Map<String, Any> =
        mapOf(
            "timestamp" to Instant.now().toString(),
            "status" to 502,
            "error" to "Bad Gateway",
            "message" to ex.message.orEmpty(),
        )

    @ExceptionHandler(UnauthorizedException::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleUnauthorized(ex: UnauthorizedException): Map<String, Any> =
        mapOf(
            "timestamp" to Instant.now().toString(),
            "status" to 401,
            "error" to "Unauthorized",
            "message" to ex.message.orEmpty(),
        )

    @ExceptionHandler(ForbiddenException::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleForbidden(ex: ForbiddenException): Map<String, Any> =
        mapOf(
            "timestamp" to Instant.now().toString(),
            "status" to 403,
            "error" to "Forbidden",
            "message" to ex.message.orEmpty(),
        )

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
    fun handleGeneric(ex: Exception): Map<String, Any> {
        logger.error("Unhandled exception", ex)

        return mapOf(
            "timestamp" to Instant.now().toString(),
            "status" to 500,
            "error" to "Internal Server Error",
            "message" to "Unexpected error occurred",
        )
    }
}
