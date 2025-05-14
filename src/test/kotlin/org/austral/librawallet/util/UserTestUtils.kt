package org.austral.librawallet.util

import org.austral.librawallet.auth.entity.User
import org.austral.librawallet.auth.repository.UserRepository
import org.austral.librawallet.auth.util.JwtUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class UserTestUtils(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtils: JwtUtil,
) {

    fun createUserAndToken(email: String, password: String): Pair<User, String> {
        val hashed = passwordEncoder.encode(password)
        val user = userRepository.save(User(email = email, password = hashed))
        val token = "Bearer ${jwtUtils.generateToken(user)}"
        return user to token
    }
} 
