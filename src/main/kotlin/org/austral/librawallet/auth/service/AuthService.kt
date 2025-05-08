package org.austral.librawallet.auth.service

import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.auth.dto.LoginRequest
import org.austral.librawallet.auth.dto.RegisterRequest
import org.austral.librawallet.auth.entity.User
import org.austral.librawallet.auth.exception.ConflictException
import org.austral.librawallet.auth.exception.UnauthorizedException
import org.austral.librawallet.auth.repository.UserRepository
import org.austral.librawallet.auth.util.JwtUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
) {

    fun register(request: RegisterRequest): User {
        if (userRepository.findByEmail(request.email).isPresent) {
            throw ConflictException("Email already registered")
        }
        val hashed = passwordEncoder.encode(request.password)
        val user = User(email = request.email, password = hashed)
        val account = Account(user = user, balance = 0L)
        user.account = account

        return userRepository.save(user)
    }

    fun login(request: LoginRequest): String {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { UnauthorizedException("Invalid credentials") }
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw UnauthorizedException("Invalid credentials")
        }
        return jwtUtil.generateToken(user)
    }

//    fun isJwtUserIdValid(jwtUserId: String): Boolean {
//        val userIdLong = jwtUserId.toLongOrNull()
//            ?: throw ForbiddenException("Invalid user ID format")
//        if (senderAccount.user.id != userIdLong) {
//            throw ForbiddenException()
//        }
//    }
}