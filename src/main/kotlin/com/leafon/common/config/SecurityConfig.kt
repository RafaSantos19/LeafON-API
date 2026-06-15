package com.leafon.common.config

import com.leafon.auth.security.AuthenticatedUser
import com.leafon.auth.security.BearerTokenExtractor
import com.leafon.auth.security.SupabaseJwtConverter
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.Cache
import org.springframework.cache.concurrent.ConcurrentMapCache
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
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
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration
import java.time.Instant

@Configuration
@EnableMethodSecurity
class SecurityConfig {
    companion object {
        const val AUTHENTICATED_UID_ATTRIBUTE = "authenticatedUid"
    }

    @Bean
    fun securityFilterChain(
        http: HttpSecurity,
        jwtDecoder: JwtDecoder,
        authenticationEntryPoint: AuthenticationEntryPoint,
        bearerTokenResolver: BearerTokenResolver,
        accessDeniedHandler: AccessDeniedHandler,
        authenticatedUidFilter: OncePerRequestFilter,
        supabaseJwtConverter: SupabaseJwtConverter,
    ): SecurityFilterChain {
        http
            .cors { }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .logout { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .addFilterAfter(authenticatedUidFilter, BearerTokenAuthenticationFilter::class.java)
            .authorizeHttpRequests {
                it
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/users/me").permitAll()
                    .requestMatchers(HttpMethod.PUT, "/users/me").permitAll()
                    .requestMatchers(HttpMethod.DELETE, "/users/me").permitAll()
                    .requestMatchers(
                        "/auth/**",
                        "/health",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                    ).permitAll()
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
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration().apply {
            allowedOriginPatterns = listOf(
                "http://localhost:*",
                "http://127.0.0.1:*",
            )
            allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
            allowedHeaders = listOf(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.ACCEPT,
                HttpHeaders.ORIGIN,
            )
            allowCredentials = false
            maxAge = 3600
        }

        return UrlBasedCorsConfigurationSource().also {
            it.registerCorsConfiguration("/**", config)
        }
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
                    ?: BearerTokenExtractor.extract(authorization)
            } catch (_: OAuth2AuthenticationException) {
                BearerTokenExtractor.extract(authorization)
            }
        }
    }

    @Bean
    fun jwtDecoder(
        @Value("\${leafon.security.supabase.jwt.jwk-set-uri}") jwkSetUri: String,
        jwkRestOperations: RestOperations,
        jwkSetCache: Cache,
    ): JwtDecoder =
        NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
            .restOperations(jwkRestOperations)
            .cache(jwkSetCache)
            .jwsAlgorithm(SignatureAlgorithm.ES256)
            .build()

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
        AuthenticationEntryPoint { _, response, _ ->
            writeSecurityError(
                response = response,
                status = HttpServletResponse.SC_UNAUTHORIZED,
                error = "Unauthorized",
                message = "Authentication is required",
            )
        }

    @Bean
    fun accessDeniedHandler(): AccessDeniedHandler =
        AccessDeniedHandler { _, response, _ ->
            writeSecurityError(
                response = response,
                status = HttpServletResponse.SC_FORBIDDEN,
                error = "Forbidden",
                message = "Access is denied",
            )
        }

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

            authentication.principal is Jwt ->
                (authentication.principal as Jwt).subject

            authentication.name.isNotBlank() ->
                authentication.name

            else -> null
        }
    }

    private fun writeSecurityError(
        response: HttpServletResponse,
        status: Int,
        error: String,
        message: String,
    ) {
        response.status = status
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = Charsets.UTF_8.name()
        response.writer.write(
            """{"timestamp":"${Instant.now()}","status":$status,"error":"$error","message":"$message"}""",
        )
    }
}
