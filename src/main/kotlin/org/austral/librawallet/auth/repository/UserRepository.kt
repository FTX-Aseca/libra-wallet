package org.austral.librawallet.auth.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.austral.librawallet.auth.entity.User
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
} 