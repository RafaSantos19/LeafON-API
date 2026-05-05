package com.leafon.auth.security

import org.springframework.core.convert.converter.Converter
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Component

@Component
class SupabaseJwtConverter : Converter<Jwt, AbstractAuthenticationToken> {

    override fun convert(jwt: Jwt): AbstractAuthenticationToken {
        val authenticatedUser = AuthenticatedUser.from(jwt)
        val authorities = buildList {
            authenticatedUser.role
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.let { add(SimpleGrantedAuthority("ROLE_${it.uppercase()}")) }
        }

        return JwtAuthenticationToken(jwt, authorities, authenticatedUser.sub).apply {
            details = authenticatedUser
        }
    }
}
