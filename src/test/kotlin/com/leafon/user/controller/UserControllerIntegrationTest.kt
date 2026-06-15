package com.leafon.user.controller

import com.leafon.user.entity.User
import com.leafon.user.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Instant
import java.util.UUID
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {
    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var userRepository: UserRepository

    @MockitoBean
    private lateinit var jwtDecoder: JwtDecoder

    private val userId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        userRepository.deleteAll()
        userRepository.save(
            User(
                id = userId,
                email = "perfil@leafon.test",
                name = "Perfil LeafON",
                phone = "11999999999",
            ),
        )
    }

    @Test
    fun `returns authenticated user profile`() {
        val token = "valid-profile-token"
        whenever(jwtDecoder.decode(token)).thenReturn(
            Jwt.withTokenValue(token)
                .header("alg", "ES256")
                .subject(userId.toString())
                .claim("aud", listOf("authenticated"))
                .claim("role", "authenticated")
                .issuedAt(Instant.now().minusSeconds(60))
                .expiresAt(Instant.now().plusSeconds(300))
                .build(),
        )
        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:$port/users/me"))
            .header("Authorization", "Bearer $token")
            .GET()
            .build()
        val response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString())

        assertEquals(200, response.statusCode())
        assertTrue(response.body().contains(""""id":"$userId""""))
        assertTrue(response.body().contains(""""email":"perfil@leafon.test""""))
        assertTrue(response.body().contains(""""name":"Perfil LeafON""""))
        assertTrue(response.body().contains(""""phone":"11999999999""""))
    }

    @Test
    fun `returns profile when authenticated principal resolves to email`() {
        val token = "email-principal-token"
        whenever(jwtDecoder.decode(token)).thenReturn(
            Jwt.withTokenValue(token)
                .header("alg", "ES256")
                .subject("perfil@leafon.test")
                .claim("aud", listOf("authenticated"))
                .claim("role", "authenticated")
                .claim("email", "perfil@leafon.test")
                .issuedAt(Instant.now().minusSeconds(60))
                .expiresAt(Instant.now().plusSeconds(300))
                .build(),
        )
        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:$port/users/me"))
            .header("Authorization", "Bearer $token")
            .GET()
            .build()
        val response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString())

        assertEquals(200, response.statusCode())
        assertTrue(response.body().contains(""""id":"$userId""""))
        assertTrue(response.body().contains(""""email":"perfil@leafon.test""""))
    }

    @Test
    fun `returns profile when authorization header contains duplicated bearer value`() {
        val token = "aaa.bbb.ccc"
        whenever(jwtDecoder.decode(token)).thenReturn(
            Jwt.withTokenValue(token)
                .header("alg", "ES256")
                .subject(userId.toString())
                .claim("aud", listOf("authenticated"))
                .claim("role", "authenticated")
                .issuedAt(Instant.now().minusSeconds(60))
                .expiresAt(Instant.now().plusSeconds(300))
                .build(),
        )
        val request = HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:$port/users/me"))
            .header("Authorization", "Bearer $token, Bearer $token")
            .GET()
            .build()
        val response = HttpClient.newHttpClient()
            .send(request, HttpResponse.BodyHandlers.ofString())

        assertEquals(200, response.statusCode())
        assertTrue(response.body().contains(""""id":"$userId""""))
        assertTrue(response.body().contains(""""email":"perfil@leafon.test""""))
    }
}
