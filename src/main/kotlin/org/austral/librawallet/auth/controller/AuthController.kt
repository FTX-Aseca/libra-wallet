package org.austral.librawallet.auth.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.austral.librawallet.auth.dto.LoginRequest
import org.austral.librawallet.auth.dto.LoginResponse
import org.austral.librawallet.auth.dto.RegisterRequest
import org.austral.librawallet.auth.dto.RegisterResponse
import org.austral.librawallet.auth.service.AuthService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/register")
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with email and password",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "User registered successfully",
                content = [Content(schema = Schema(implementation = RegisterResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request data or email already exists",
            ),
        ],
    )
    fun register(
        @Valid @RequestBody
        request: RegisterRequest,
    ): ResponseEntity<RegisterResponse> {
        val user = authService.register(request)
        val response = RegisterResponse(id = user.id!!, email = user.email)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticates user credentials and returns a JWT token",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Login successful",
                content = [Content(schema = Schema(implementation = LoginResponse::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Invalid credentials",
            ),
        ],
    )
    fun login(
        @Valid @RequestBody
        request: LoginRequest,
    ): ResponseEntity<LoginResponse> {
        val token = authService.login(request)
        return ResponseEntity.ok(LoginResponse(token))
    }
}
