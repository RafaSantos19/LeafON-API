package com.leafon.common.exception

class ExternalServiceException(
    message: String,
    cause: Throwable? = null,
) : RuntimeException(message, cause)
