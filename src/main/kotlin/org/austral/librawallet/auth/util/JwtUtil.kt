package org.austral.librawallet.auth.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import org.austral.librawallet.auth.entity.User

@Component
class JwtUtil(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.expirationMs}") private val expirationMs: Long
) {

    fun generateToken(user: User): String {
        val now = Date()
        val expiryDate = Date(now.time + expirationMs)
        return JWT.create()
            .withSubject(user.id.toString())
            .withExpiresAt(expiryDate)
            .sign(Algorithm.HMAC256(secret))
    }

    fun validateTokenAndGetSubject(token: String): String {
        val verifier = JWT.require(Algorithm.HMAC256(secret)).build()
        val decodedJWT = verifier.verify(token)
        return decodedJWT.subject
    }
} 