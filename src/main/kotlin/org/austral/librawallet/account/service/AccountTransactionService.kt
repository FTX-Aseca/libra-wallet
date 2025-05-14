package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.TransactionRequest
import org.austral.librawallet.account.dto.TransactionResponse
import org.austral.librawallet.account.entity.Transaction
import org.austral.librawallet.account.entity.TransactionType
import org.austral.librawallet.account.exceptions.ForbiddenException
import org.austral.librawallet.account.exceptions.NotFoundException
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.account.repository.TransactionRepository
import org.austral.librawallet.auth.exception.ConflictException
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

        val amountInCents = formattedDoubleToCents(request.amount)

        if (request.type == org.austral.librawallet.account.entity.TransactionType.EXPENSE && account.balance < amountInCents) {
            throw ConflictException("Insufficient funds")
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
                    throw ForbiddenException("Account does not belong to authenticated user")
                }
            } ?: throw NotFoundException("Account not found")
        } ?: throw ForbiddenException("Invalid user ID format")
}
