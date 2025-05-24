package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.transaction.TransactionResponse
import org.austral.librawallet.account.dto.transfer.TransferRequest
import org.austral.librawallet.account.dto.transfer.TransferResponse
import org.austral.librawallet.account.entity.Account
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
class TransferService(
    private val accountRepository: AccountRepository,
    private val accountService: AccountService,
    private val transactionRepository: TransactionRepository,
) {

    @Transactional
    fun transfer(request: TransferRequest, jwtUserId: String): TransferResponse {
        if (request.amount <= 0) {
            throw BadRequestException(ErrorMessages.INVALID_AMOUNT)
        }

        val senderAccount = getSenderAccountOrThrow(jwtUserId)
        val recipientAccount = accountService.getAccountOrThrow(request.identifierType, request.toIdentifier)
        val amountInCents = formattedDoubleToCents(request.amount)

        if (senderAccount.balance < amountInCents) {
            throw ConflictException(ErrorMessages.INSUFFICIENT_FUNDS)
        }

        updateBalances(senderAccount, amountInCents, recipientAccount)
        recordTransactions(senderAccount, recipientAccount, amountInCents)

        return getTransferResponse(senderAccount, recipientAccount)
    }

    private fun getTransferResponse(
        senderAccount: Account,
        recipientAccount: Account,
    ): TransferResponse {
        val fromBalance = centsToFormattedDouble(senderAccount.balance)
        val toBalance = centsToFormattedDouble(recipientAccount.balance)
        return TransferResponse(fromBalance = fromBalance, toBalance = toBalance)
    }

    private fun recordTransactions(
        senderAccount: Account,
        recipientAccount: Account,
        amountInCents: Long,
    ) {
        val expense = Transaction(
            account = senderAccount,
            otherAccount = recipientAccount,
            type = TransactionType.EXPENSE,
            amount = amountInCents,
        )
        val income = Transaction(
            account = recipientAccount,
            otherAccount = senderAccount,
            type = TransactionType.INCOME,
            amount = amountInCents,
        )
        transactionRepository.save(expense)
        transactionRepository.save(income)
    }

    private fun updateBalances(
        senderAccount: Account,
        amountInCents: Long,
        recipientAccount: Account,
    ) {
        senderAccount.balance = senderAccount.balance - amountInCents
        recipientAccount.balance = recipientAccount.balance + amountInCents
        accountRepository.save(senderAccount)
        accountRepository.save(recipientAccount)
    }

    fun getTransactions(jwtUserId: String): List<TransactionResponse> {
        val userIdLong = jwtUserId.toLongOrNull()
            ?: throw ForbiddenException(ErrorMessages.INVALID_USER_ID_FORMAT)
        val account = accountRepository.findByUserId(userIdLong)
            ?: throw NotFoundException(ErrorMessages.ACCOUNT_NOT_FOUND)
        val transactions = transactionRepository.findByAccountId(account.id!!)
        return transactions.map { tx ->
            TransactionResponse(
                type = tx.type,
                amount = centsToFormattedDouble(tx.amount),
                timestamp = tx.timestamp,
                description = tx.description,
            )
        }
    }

    private fun getSenderAccountOrThrow(jwtUserId: String): Account {
        val userIdLong = jwtUserId.toLongOrNull()
            ?: throw ForbiddenException(ErrorMessages.INVALID_USER_ID_FORMAT)
        val senderAccount = accountRepository.findByUserId(userIdLong)
            ?: throw NotFoundException(ErrorMessages.SENDER_ACCOUNT_NOT_FOUND)
        return senderAccount
    }
}
