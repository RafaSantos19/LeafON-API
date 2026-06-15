package com.leafon.auth.security

import com.leafon.common.exception.UnauthorizedException
import java.util.UUID

fun String.toAuthenticatedUserId(): UUID =
    runCatching { UUID.fromString(trim()) }
        .getOrElse { throw UnauthorizedException("Authenticated UID is not a valid UUID") }
