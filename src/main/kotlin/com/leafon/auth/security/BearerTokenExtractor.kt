package com.leafon.auth.security

object BearerTokenExtractor {
    private val jwtPattern = Regex("""[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+\.[A-Za-z0-9_-]+""")

    fun extract(authorization: String?): String? {
        val value = authorization?.trim().orEmpty()
        if (value.isBlank()) {
            return null
        }

        if (value.startsWith("Bearer ", ignoreCase = true)) {
            value.removePrefix("Bearer ")
                .substringBefore(',')
                .trim()
                .takeIf(::looksLikeJwt)
                ?.let { return it }
        }

        return jwtPattern.find(value)?.value
    }

    private fun looksLikeJwt(value: String): Boolean =
        value.count { it == '.' } == 2 && !value.contains(' ')
}
