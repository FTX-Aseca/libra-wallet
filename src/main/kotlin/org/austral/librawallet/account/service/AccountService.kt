package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.AccountDetails
import org.austral.librawallet.account.dto.IdentifierType
import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.account.exceptions.ForbiddenException
import org.austral.librawallet.account.exceptions.NotFoundException
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.shared.constants.ErrorMessages
import org.austral.librawallet.shared.formatters.centsToFormattedDouble
import org.springframework.stereotype.Service

@Service
class AccountService(
    private val accountRepository: AccountRepository,
) {
    fun getBalance(accountId: Long, userId: String): Double {
        val account = getAccount(accountId)
        validateAccount(account, userId)
        return centsToFormattedDouble(account.balance)
    }

    fun getAccountDetails(
        accountId: Long,
        userId: String,
    ): AccountDetails {
        val account = getAccount(accountId)
        validateAccount(account, userId)
        return AccountDetails(
            email = account.user.email,
            alias = account.alias,
            cvu = account.cvu,
        )
    }

    fun getAccountOrThrow(identifierType: IdentifierType, identifier: String): Account {
        return when (identifierType) {
            IdentifierType.ALIAS -> accountRepository.findByAlias(identifier)
                ?: throw NotFoundException(ErrorMessages.ACCOUNT_NOT_FOUND)
            IdentifierType.CVU -> accountRepository.findByCvu(identifier)
                ?: throw NotFoundException(ErrorMessages.ACCOUNT_NOT_FOUND)
        }
    }

    private fun getAccount(accountId: Long): Account {
        return accountRepository.findById(accountId)
            .orElseThrow { NotFoundException(ErrorMessages.ACCOUNT_NOT_FOUND) }
    }

    private fun validateAccount(
        account: Account,
        userId: String,
    ) {
        val userIdLong = userId.toLongOrNull()
            ?: throw ForbiddenException(ErrorMessages.INVALID_USER_ID_FORMAT)

        if (account.user.id != userIdLong) {
            throw ForbiddenException()
        }
    }
}
