package org.austral.librawallet.util

import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.account.repository.DebinRequestRepository
import org.austral.librawallet.account.repository.TopUpOrderRepository
import org.austral.librawallet.account.repository.TransactionRepository
import org.austral.librawallet.auth.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class DatabaseInitializationService(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val debinRequestRepository: DebinRequestRepository,
    private val topUpOrderRepository: TopUpOrderRepository,
) {
    fun clean() {
        debinRequestRepository.deleteAll()
        topUpOrderRepository.deleteAll()
        transactionRepository.deleteAll()
        accountRepository.deleteAll()
        userRepository.deleteAll()
    }
}
