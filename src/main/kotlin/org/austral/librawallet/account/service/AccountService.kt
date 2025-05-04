package org.austral.librawallet.account.service

import org.austral.librawallet.account.exceptions.ForbiddenException
import org.austral.librawallet.account.exceptions.NotFoundException
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.account.util.centsToFormattedDouble
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val accountRepository: AccountRepository,
) {
    fun getBalance(accountId: Long, userId: String): Double {
        val account = accountRepository.findById(accountId)
            .orElseThrow { NotFoundException("Account not found") }
        if (account.user.id.toString() != userId) {
            throw ForbiddenException()
        }
        return centsToFormattedDouble(account.balance)
    }
}