package org.austral.librawallet.util

import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.account.repository.TransactionRepository
import org.austral.librawallet.auth.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class DatabaseInitializationService(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
) {
    fun clean() {
        transactionRepository.deleteAll()
        accountRepository.deleteAll()
        userRepository.deleteAll()
    }
}