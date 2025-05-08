package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.IdentifierType
import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.account.exceptions.ForbiddenException
import org.austral.librawallet.account.exceptions.NotFoundException
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.shared.formatters.centsToFormattedDouble
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val accountRepository: AccountRepository,
) {
    fun getBalance(accountId: Long, userId: String): Double {
        val account = accountRepository.findById(accountId)
            .orElseThrow { NotFoundException("Account not found") }

        val userIdLong = userId.toLongOrNull()
            ?: throw ForbiddenException("Invalid user ID format")

        if (account.user.id != userIdLong) {
            throw ForbiddenException()
        }
        return centsToFormattedDouble(account.balance)
    }

    fun getAccountOrThrow(identifierType: IdentifierType, receiverIdentifier: String): Account {
        return when (identifierType) {
            IdentifierType.ALIAS -> accountRepository.findByAlias(receiverIdentifier)
            IdentifierType.CVU -> accountRepository.findByCvu(receiverIdentifier)
        } ?: throw NotFoundException("Recipient account not found")
    }
}
