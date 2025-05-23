package org.austral.librawallet.auth.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.auth.dto.LoginRequest
import org.austral.librawallet.auth.dto.RegisterRequest
import org.austral.librawallet.auth.entity.User
import org.austral.librawallet.auth.service.AuthService
import org.austral.librawallet.auth.config.SecurityConfig
import org.austral.librawallet.auth.exception.ConflictException
import org.austral.librawallet.shared.constants.ErrorMessages
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(AuthController::class)
@Import(SecurityConfig::class)
class AuthControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var authService: AuthService

    @Test
    fun `register should return 201 when registration is successful`() {
        // Given
        val request = RegisterRequest("test@example.com", "Password123!")
        val user = User(id = 1L, email = request.email, password = "hashedPassword")
        val account = Account(id = 1L, user = user, balance = 0L)
        user.account = account
        whenever(authService.register(any())).thenReturn(user)

        // When/Then
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.password").doesNotExist())
    }

    @Test
    fun `register should return 409 when email is already registered`() {
        // Given
        val request = RegisterRequest("existing@example.com", "Password123!")
        whenever(authService.register(any())).thenThrow(ConflictException(ErrorMessages.EMAIL_ALREADY_REGISTERED))

        // When/Then
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value(ErrorMessages.EMAIL_ALREADY_REGISTERED))
    }

    @Test
    fun `login should return 200 and token when credentials are valid`() {
        // Given
        val request = LoginRequest("test@example.com", "password123")
        val token = "jwt.token.here"
        whenever(authService.login(any())).thenReturn(token)

        // When/Then
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").value(token))
    }
} 