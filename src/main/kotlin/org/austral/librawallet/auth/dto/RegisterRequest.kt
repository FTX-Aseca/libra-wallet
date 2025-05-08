package org.austral.librawallet.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    @field:Pattern(regexp = "^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\\W).+$", message = "Password must contain letters, numbers, and special characters")
    val password: String,
)
