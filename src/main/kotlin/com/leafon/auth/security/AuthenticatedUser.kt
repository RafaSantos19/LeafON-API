package com.leafon.auth.security

import com.leafon.common.exception.UnauthorizedException
import org.springframework.security.oauth2.jwt.Jwt

data class AuthenticatedUser(
    val sub: String,
    val email: String?,
    val name: String?,
    val role: String?,
    val audience: List<String>,
    val emailVerified: Boolean?,
) {
    companion object {
        fun from(jwt: Jwt): AuthenticatedUser {
            val userMetadata = jwt.getClaimAsMap("user_metadata").orEmpty()

            return AuthenticatedUser(
                sub = jwt.subject ?: throw UnauthorizedException("JWT subject is missing"),
                email = jwt.getClaimAsString("email").normalized(),
                name = (
                    jwt.getClaimAsString("name").normalized()
                        ?: userMetadata.stringValue("name")
                        ?: userMetadata.stringValue("full_name")
                    ),
                role = jwt.getClaimAsString("role").normalized(),
                audience = jwt.audience.filter { it.isNotBlank() },
                emailVerified = jwt.claims.booleanValue("email_verified")
                    ?: userMetadata.booleanValue("email_verified"),
            )
        }

        private fun String?.normalized(): String? =
            this?.trim()?.takeIf { it.isNotBlank() }

        private fun Map<String, Any>.stringValue(key: String): String? =
            this[key]?.toString()?.trim()?.takeIf { it.isNotBlank() }

        private fun Map<String, Any>.booleanValue(key: String): Boolean? =
            when (val value = this[key]) {
                is Boolean -> value
                is String -> value.toBooleanStrictOrNull()
                else -> null
            }
    }
}
