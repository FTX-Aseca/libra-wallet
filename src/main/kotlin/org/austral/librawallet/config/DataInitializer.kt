package org.austral.librawallet.config

import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.auth.entity.User
import org.austral.librawallet.auth.repository.UserRepository
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class DataInitializer(
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val passwordEncoder: PasswordEncoder,
) : ApplicationRunner {
    override fun run(args: ApplicationArguments?) {
        if (userRepository.count() == 0L) {
            val user1 = userRepository.save(
                User(email = "alice@example.com", password = passwordEncoder.encode("Password1!")),
            )
            accountRepository.save(
                Account(
                    user = user1,
                    balance = 10_000L,
                    alias = "sunny.river.001",
                    cvu = "0123456789012345678901",
                ),
            )
            val user2 = userRepository.save(
                User(email = "bob@example.com", password = passwordEncoder.encode("Password2!")),
            )
            accountRepository.save(
                Account(
                    user = user2,
                    balance = 5_000L,
                    alias = "happy.mountain.002",
                    cvu = "1098765432109876543210",
                ),
            )
            println("DataInitializer: Data initialized")
        } else {
            println("DataInitializer: Data already initialized")
        }
    }
}
