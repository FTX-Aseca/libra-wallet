package org.austral.librawallet.auth.controller

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
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/register")
    fun register(
        @Valid @RequestBody
        request: RegisterRequest,
    ): ResponseEntity<RegisterResponse> {
        val user = authService.register(request)
        val response = RegisterResponse(id = user.id!!, email = user.email)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/login")
    fun login(
        @Valid @RequestBody
        request: LoginRequest,
    ): ResponseEntity<LoginResponse> {
        val token = authService.login(request)
        return ResponseEntity.ok(LoginResponse(token))
    }
}
