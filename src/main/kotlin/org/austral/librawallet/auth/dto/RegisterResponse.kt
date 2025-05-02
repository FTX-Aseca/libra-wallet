package org.austral.librawallet.auth.dto

/**
 * Response payload for successful registration.
 * @param id the newly created user ID
 * @param email the registered user email address
 */
data class RegisterResponse(
    val id: Long,
    val email: String
)
