package com.leafon.common.config

import com.leafon.auth.security.AuthenticatedUser
import com.leafon.auth.security.SupabaseJwtConverter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.Cache
import org.springframework.cache.concurrent.ConcurrentMapCache
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.BadJwtException
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.JwtException
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.web.client.RestOperations
import org.springframework.web.client.RestTemplate
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.Duration
import java.util.Base64

@Configuration
@EnableMethodSecurity
class SecurityConfig {
    private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)

    companion object {
        const val AUTHENTICATED_UID_ATTRIBUTE = "authenticatedUid"
        private const val AUTHORIZATION_ATTRIBUTE = "leafon.security.receivedAuthorization"
        private const val TOKEN_ATTRIBUTE = "leafon.security.receivedToken"
        private const val AUTH_RESULT_ATTRIBUTE = "leafon.security.tokenDecodeResult"
        private const val AUTH_RESULT_HEADER = "X-Token-Decode-Result"
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtDecoder: JwtDecoder,
        authenticationEntryPoint: AuthenticationEntryPoint,
        bearerTokenResolver: BearerTokenResolver,
        accessDeniedHandler: AccessDeniedHandler,
        authDebugFilter: OncePerRequestFilter,
        authenticatedUidFilter: OncePerRequestFilter,
        supabaseJwtConverter: SupabaseJwtConverter,
    ): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .logout { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterBefore(authDebugFilter, BearerTokenAuthenticationFilter::class.java)
            .addFilterAfter(authenticatedUidFilter, BearerTokenAuthenticationFilter::class.java)
            .authorizeHttpRequests {
                it
                    .requestMatchers("/auth/**", "/health").permitAll()
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer {
                it
                    .bearerTokenResolver(bearerTokenResolver)
                    .jwt { jwt ->
                        jwt
                            .decoder(jwtDecoder)
                            .jwtAuthenticationConverter(supabaseJwtConverter)
                    }
                    .authenticationEntryPoint(authenticationEntryPoint)
            }
            .exceptionHandling {
                it
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler)
            }

        return http.build()
    }

    @Bean
    fun bearerTokenResolver(): BearerTokenResolver {
        val delegate = DefaultBearerTokenResolver()

        return BearerTokenResolver { request ->
            val authorization = request.getHeader(HttpHeaders.AUTHORIZATION)

            if (authorization.isNullOrBlank()) {
                return@BearerTokenResolver null
            }

            try {
                delegate.resolve(request)
                    ?: authorization.trim().takeIf(::looksLikeJwt)
            } catch (_: OAuth2AuthenticationException) {
                authorization.trim().takeIf(::looksLikeJwt)
            }
        }
    }

    @Bean
    fun jwtDecoder(
        @Value("\${leafon.security.supabase.jwt.jwk-set-uri}") jwkSetUri: String,
        jwkRestOperations: RestOperations,
        jwkSetCache: Cache,
    ): JwtDecoder {
        val delegate = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
            .restOperations(jwkRestOperations)
            .cache(jwkSetCache)
            .jwsAlgorithm(SignatureAlgorithm.ES256)
            .build()

        return JwtDecoder { token ->
            try {
                delegate.decode(token).also { jwt ->
                    registerDecodeResult(buildDecodeSuccessMessage(jwt))
                }
            } catch (ex: JwtException) {
                val message = buildDecodeFailureMessage(token, ex)
                registerDecodeResult(message, exception = ex)
                throw BadJwtException(message, ex)
            }
        }
    }

    @Bean
    fun jwkRestOperations(): RestOperations =
        RestTemplate().apply {
            requestFactory = org.springframework.http.client.SimpleClientHttpRequestFactory().apply {
                setConnectTimeout(Duration.ofSeconds(5))
                setReadTimeout(Duration.ofSeconds(20))
            }
        }

    @Bean
    fun jwkSetCache(): Cache = ConcurrentMapCache("supabase-jwk-set")

    @Bean
    fun authDebugFilter(
        bearerTokenResolver: BearerTokenResolver,
    ): OncePerRequestFilter =
        object : OncePerRequestFilter() {
            override fun doFilterInternal(
                request: HttpServletRequest,
                response: HttpServletResponse,
                filterChain: FilterChain,
            ) {
                val authorization = request.getHeader(HttpHeaders.AUTHORIZATION)
                request.setAttribute(AUTHORIZATION_ATTRIBUTE, authorization ?: "")

                if (authorization.isNullOrBlank()) {
                    storeAuthDebug(
                        request = request,
                        response = response,
                        token = null,
                        message = "Nenhum header Authorization foi recebido.",
                        warnOnly = true,
                    )
                    filterChain.doFilter(request, response)
                    return
                }

                val token = try {
                    bearerTokenResolver.resolve(request)
                } catch (ex: OAuth2AuthenticationException) {
                    storeAuthDebug(
                        request = request,
                        response = response,
                        token = null,
                        message = "Falha ao extrair Bearer token do header Authorization: ${ex.message ?: "formato invalido"}",
                        exception = ex,
                    )
                    filterChain.doFilter(request, response)
                    return
                }

                if (token.isNullOrBlank()) {
                    storeAuthDebug(
                        request = request,
                        response = response,
                        token = token,
                        message = "O header Authorization foi recebido, mas nenhum token Bearer valido foi extraido.",
                        warnOnly = true,
                    )
                    filterChain.doFilter(request, response)
                    return
                }

                filterChain.doFilter(request, response)
            }
        }

    @Bean
    fun authenticatedUidFilter(): OncePerRequestFilter =
        object : OncePerRequestFilter() {
            override fun doFilterInternal(
                request: HttpServletRequest,
                response: HttpServletResponse,
                filterChain: FilterChain,
            ) {
                resolveAuthenticatedUid()?.let { uid ->
                    request.setAttribute(AUTHENTICATED_UID_ATTRIBUTE, uid)
                }

                filterChain.doFilter(request, response)
            }
        }

    @Bean
    fun authenticationEntryPoint(): AuthenticationEntryPoint =
        AuthenticationEntryPoint { request, response, exception ->
            writeUnauthorized(
                request = request,
                response = response,
                fallbackMessage = buildExceptionDetail(exception),
            )
        }

    @Bean
    fun accessDeniedHandler(): AccessDeniedHandler =
        AccessDeniedHandler { request, response, exception ->
            writeUnauthorized(
                request = request,
                response = response,
                fallbackMessage = buildExceptionDetail(exception),
            )
        }

    private fun buildExceptionDetail(exception: Exception): String =
        buildString {
            append(exception::class.simpleName ?: "AuthenticationException")
            exception.message
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    append(": ")
                    append(it)
                }
            exception.cause?.message
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    append(" | cause: ")
                        append(it)
                }
        }

    private fun buildDecodeSuccessMessage(jwt: Jwt): String =
        buildString {
            append("Decode do token realizado com sucesso")
            append(". sub=")
            append(jwt.subject ?: "desconhecido")
            jwt.issuer?.toString()
                ?.takeIf { it.isNotBlank() }
                ?.let {
                    append(", iss=")
                    append(it)
                }
            jwt.expiresAt
                ?.let {
                    append(", exp=")
                    append(it)
                }
        }

    private fun buildDecodeFailureMessage(
        token: String,
        ex: JwtException,
    ): String =
        buildString {
            append("Falha ao decodificar token: ")
            append(ex.message ?: ex::class.simpleName ?: "erro desconhecido")
            decodeJwtParts(token)
                ?.let {
                    append(" | decode local=")
                    append(it)
                }
        }

    private fun decodeJwtParts(token: String): String? {
        val parts = token.split('.')
        if (parts.size != 3) {
            return null
        }

        val header = decodeJwtSection(parts[0]) ?: return null
        val payload = decodeJwtSection(parts[1]) ?: return null
        return """{"header":${header.quoteForJson()},"payload":${payload.quoteForJson()}}"""
    }

    private fun decodeJwtSection(value: String): String? =
        runCatching {
            val normalized = value.padEnd(value.length + (4 - value.length % 4) % 4, '=')
            String(Base64.getUrlDecoder().decode(normalized), StandardCharsets.UTF_8)
        }.getOrNull()

    private fun looksLikeJwt(value: String): Boolean =
        value.count { it == '.' } == 2 && !value.contains(' ')

    private fun resolveAuthenticatedUid(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: return null

        if (!authentication.isAuthenticated || authentication is AnonymousAuthenticationToken) {
            return null
        }

        return when {
            authentication.details is AuthenticatedUser ->
                (authentication.details as AuthenticatedUser).sub

            authentication is JwtAuthenticationToken ->
                authentication.token.subject

            authentication.name.isNotBlank() ->
                authentication.name

            else -> null
        }
    }

    private fun registerDecodeResult(
        message: String,
        exception: Exception? = null,
    ) {
        val request = currentRequest() ?: return
        request.setAttribute(AUTH_RESULT_ATTRIBUTE, message)

        if (exception != null) {
            logger.warn(message, exception)
        } else {
            logger.info(message)
        }
    }

    private fun storeAuthDebug(
        request: HttpServletRequest,
        response: HttpServletResponse,
        token: String?,
        message: String,
        warnOnly: Boolean = false,
        exception: Exception? = null,
    ) {
        request.setAttribute(TOKEN_ATTRIBUTE, token ?: "")
        request.setAttribute(AUTH_RESULT_ATTRIBUTE, message)
        response.setHeader(AUTH_RESULT_HEADER, message)

        when {
            exception != null -> logger.warn(message, exception)
            warnOnly -> logger.warn(message)
            else -> logger.info(message)
        }
    }

    private fun currentRequest(): HttpServletRequest? =
        (RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes)?.request

    private fun writeUnauthorized(
        request: HttpServletRequest,
        response: HttpServletResponse,
        fallbackMessage: String,
    ) {
        val authorization = request.getAttribute(AUTHORIZATION_ATTRIBUTE)?.toString().orEmpty()
        val token = request.getAttribute(TOKEN_ATTRIBUTE)?.toString().orEmpty()
        val decodeResult = request.getAttribute(AUTH_RESULT_ATTRIBUTE)?.toString()
            ?.takeIf { it.isNotBlank() }
            ?: fallbackMessage

        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()
        response.setHeader(AUTH_RESULT_HEADER, decodeResult)
        response.writer.write(
            """
            {"timestamp":"${Instant.now()}","status":401,"error":"Unauthorized","message":"${decodeResult.escapeJson()}","receivedAuthorization":"${authorization.escapeJson()}","receivedToken":"${token.escapeJson()}","decodeResult":"${decodeResult.escapeJson()}"}
            """.trimIndent(),
        )
    }

    private fun String.quoteForJson(): String = "\"${escapeJson()}\""

    private fun String.escapeJson(): String =
        replace("\\", "\\\\").replace("\"", "\\\"")
}
