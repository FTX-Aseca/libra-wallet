package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.transaction.TransactionRequest
import org.austral.librawallet.account.dto.transaction.TransactionResponse
import org.austral.librawallet.account.entity.Transaction
import org.austral.librawallet.account.entity.TransactionType
import org.austral.librawallet.account.exceptions.BadRequestException
import org.austral.librawallet.account.exceptions.ForbiddenException
import org.austral.librawallet.account.exceptions.NotFoundException
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.account.repository.TransactionRepository
import org.austral.librawallet.auth.exception.ConflictException
import org.austral.librawallet.shared.constants.ErrorMessages
import org.austral.librawallet.shared.formatters.centsToFormattedDouble
import org.austral.librawallet.shared.formatters.formattedDoubleToCents
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AccountTransactionService(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository,
) {

    fun getAccountTransactions(accountId: Long, jwtUserId: String): List<TransactionResponse> {
        validateAccount(accountId, jwtUserId)

        val transactions = transactionRepository.findByAccountIdOrderByTimestampDesc(accountId)
        return transactions.map { tx ->
            TransactionResponse(
                type = tx.type,
                amount = centsToFormattedDouble(tx.amount),
                timestamp = tx.timestamp,
                description = tx.description,
            )
        }
    }

    @Transactional
    fun createTransaction(accountId: Long, request: TransactionRequest, jwtUserId: String): TransactionResponse {
        val account = validateAccount(accountId, jwtUserId)

        if (request.amount <= 0) {
            throw BadRequestException(ErrorMessages.INVALID_AMOUNT)
        }

        val amountInCents = formattedDoubleToCents(request.amount)

        if (request.type == TransactionType.EXPENSE && account.balance < amountInCents) {
            throw ConflictException(ErrorMessages.INSUFFICIENT_FUNDS)
        }

        when (request.type) {
            TransactionType.INCOME -> account.balance += amountInCents
            TransactionType.EXPENSE -> account.balance -= amountInCents
        }
        accountRepository.save(account)

        val transaction = Transaction(
            account = account,
            otherAccount = account,
            type = request.type,
            amount = amountInCents,
            description = request.description,
        )

        val savedTransaction = transactionRepository.save(transaction)
        return TransactionResponse(
            type = savedTransaction.type,
            amount = centsToFormattedDouble(savedTransaction.amount),
            timestamp = savedTransaction.timestamp,
            description = savedTransaction.description,
        )
    }

    private fun validateAccount(accountId: Long, jwtUserId: String) =
        jwtUserId.toLongOrNull()?.let { userIdLong ->
            accountRepository.findById(accountId).orElse(null)?.also { account ->
                if (account.user.id != userIdLong) {
                    throw ForbiddenException(ErrorMessages.ACCOUNT_DOES_NOT_BELONG)
                }
            } ?: throw NotFoundException(ErrorMessages.ACCOUNT_NOT_FOUND)
        } ?: throw ForbiddenException(ErrorMessages.INVALID_USER_ID_FORMAT)
}
