package com.leafon.auth.service

import com.leafon.auth.security.AuthenticatedUser
import com.leafon.common.exception.UnauthorizedException
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service

@Service
class AuthContextService {

    fun getAuthenticatedUser(): AuthenticatedUser {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw UnauthorizedException()

        if (!authentication.isAuthenticated || authentication is AnonymousAuthenticationToken) {
            throw UnauthorizedException()
        }

        authentication.details
            ?.let { details -> details as? AuthenticatedUser }
            ?.let { return it }

        return when (authentication) {
            is JwtAuthenticationToken -> AuthenticatedUser.Companion.from(authentication.token)
            else -> (authentication.principal as? Jwt)
                ?.let(AuthenticatedUser.Companion::from)
                ?: throw UnauthorizedException("Unsupported authentication principal")
        }
    }
}