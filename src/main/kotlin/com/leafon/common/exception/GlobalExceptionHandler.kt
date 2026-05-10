package com.leafon.common.exception

import com.leafon.smartpot.exception.SmartPotNotFoundException
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
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

    @ExceptionHandler(SmartPotNotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleSmartPotNotFound(ex: SmartPotNotFoundException): Map<String, Any> =
        mapOf(
            "timestamp" to Instant.now().toString(),
            "status" to 404,
            "error" to "Not Found",
            "message" to ex.message.orEmpty(),
        )

    @ExceptionHandler(
        MethodArgumentNotValidException::class,
        ConstraintViolationException::class,
        MethodArgumentTypeMismatchException::class,
    )
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadRequest(ex: Exception): Map<String, Any> =
        when (ex) {
            is MethodArgumentNotValidException -> {
                val errors = ex.bindingResult.allErrors.mapNotNull { error ->
                    when (error) {
                        is FieldError -> error.field to (error.defaultMessage ?: "Invalid value")
                        else -> null
                    }
                }

                mapOf(
                    "timestamp" to Instant.now().toString(),
                    "status" to 400,
                    "error" to "Bad Request",
                    "message" to "Validation failed",
                    "errors" to errors.associate { it },
                )
            }

            is ConstraintViolationException -> mapOf(
                "timestamp" to Instant.now().toString(),
                "status" to 400,
                "error" to "Bad Request",
                "message" to ex.constraintViolations.joinToString("; ") { violation ->
                    "${violation.propertyPath}: ${violation.message}"
                },
            )

            is MethodArgumentTypeMismatchException -> mapOf(
                "timestamp" to Instant.now().toString(),
                "status" to 400,
                "error" to "Bad Request",
                "message" to "Invalid value for ${ex.name}",
            )

            else -> mapOf(
                "timestamp" to Instant.now().toString(),
                "status" to 400,
                "error" to "Bad Request",
                "message" to ex.message.orEmpty(),
            )
        }

    @ExceptionHandler(DataIntegrityViolationException::class)
    @ResponseStatus(HttpStatus.CONFLICT)
    fun handleDataIntegrityViolation(@Suppress("UNUSED_PARAMETER") ex: DataIntegrityViolationException): Map<String, Any> =
        mapOf(
            "timestamp" to Instant.now().toString(),
            "status" to 409,
            "error" to "Conflict",
            "message" to "Request conflicts with existing data",
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
