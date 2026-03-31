package com.leafon.common.exception

class NotFoundException(
    message: String = "Resource not found",
) : RuntimeException(message)
