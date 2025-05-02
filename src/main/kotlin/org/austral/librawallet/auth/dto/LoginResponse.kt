package org.austral.librawallet.auth.dto

/**
 * Response payload for successful login.
 * @param token the JWT token for authenticated sessions
 */
data class LoginResponse(
    val token: String
) 