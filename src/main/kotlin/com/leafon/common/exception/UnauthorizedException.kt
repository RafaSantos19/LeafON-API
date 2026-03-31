package com.leafon.common.exception

class UnauthorizedException(
    message: String = "Unauthorized",
) : RuntimeException(message)
