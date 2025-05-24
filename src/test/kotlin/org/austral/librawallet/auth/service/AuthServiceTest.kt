package org.austral.librawallet.auth.service

import org.austral.librawallet.auth.dto.LoginRequest
import org.austral.librawallet.auth.dto.RegisterRequest
import org.austral.librawallet.auth.entity.User
import org.austral.librawallet.auth.exception.ConflictException
import org.austral.librawallet.auth.exception.UnauthorizedException
import org.austral.librawallet.auth.repository.UserRepository
import org.austral.librawallet.auth.util.JwtUtil
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional

class AuthServiceTest {

    private val userRepository: UserRepository = mock()
    private val passwordEncoder: PasswordEncoder = mock()
    private val jwtUtil: JwtUtil = mock()
    private val authService = AuthService(userRepository, passwordEncoder, jwtUtil)

    @Test
    fun `register should create new user when email is not taken`() {
        // Given
        val request = RegisterRequest("test@example.com", "password123")
        val hashedPassword = "hashedPassword"
        whenever(userRepository.findByEmail(request.email)).thenReturn(Optional.empty())
        whenever(passwordEncoder.encode(request.password)).thenReturn(hashedPassword)

        whenever(userRepository.save(any<User>())).thenAnswer { invocation ->
            val user = invocation.arguments[0] as User
            user.id = 1L
            user
        }

        // When
        val result = authService.register(request)

        // Then
        assert(result.id == 1L)
        assert(result.email == request.email)
        assert(result.password == hashedPassword)
        assert(result.account != null)
        assert(result.account?.balance == 0L)
        verify(userRepository).save(any())
    }

    @Test
    fun `register should throw ConflictException when email is taken`() {
        // Given
        val request = RegisterRequest("test@example.com", "password123")
        val existingUser = User(id = 1L, email = request.email, password = "hashedPassword")
        whenever(userRepository.findByEmail(request.email)).thenReturn(Optional.of(existingUser))

        // When/Then
        assertThrows<ConflictException> {
            authService.register(request)
        }
    }

    @Test
    fun `login should return token when credentials are valid`() {
        // Given
        val request = LoginRequest("test@example.com", "password123")
        val user = User(id = 1L, email = request.email, password = "hashedPassword")
        val token = "jwt.token.here"
        whenever(userRepository.findByEmail(request.email)).thenReturn(Optional.of(user))
        whenever(passwordEncoder.matches(request.password, user.password)).thenReturn(true)
        whenever(jwtUtil.generateToken(user)).thenReturn(token)

        // When
        val result = authService.login(request)

        // Then
        assert(result == token)
    }

    @Test
    fun `login should throw UnauthorizedException when user not found`() {
        // Given
        val request = LoginRequest("test@example.com", "password123")
        whenever(userRepository.findByEmail(request.email)).thenReturn(Optional.empty())

        // When/Then
        assertThrows<UnauthorizedException> {
            authService.login(request)
        }
    }

    @Test
    fun `login should throw UnauthorizedException when password is invalid`() {
        // Given
        val request = LoginRequest("test@example.com", "password123")
        val user = User(id = 1L, email = request.email, password = "hashedPassword")
        whenever(userRepository.findByEmail(request.email)).thenReturn(Optional.of(user))
        whenever(passwordEncoder.matches(request.password, user.password)).thenReturn(false)

        // When/Then
        assertThrows<UnauthorizedException> {
            authService.login(request)
        }
    }
}
