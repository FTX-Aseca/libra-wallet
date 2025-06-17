package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.IdentifierType
import org.austral.librawallet.account.dto.topup.TopUpRequest
import org.austral.librawallet.account.dto.topup.TopUpResponse
import org.austral.librawallet.account.entity.TopUpOrder
import org.austral.librawallet.account.entity.Transaction
import org.austral.librawallet.account.entity.TransactionType
import org.austral.librawallet.account.exceptions.BadRequestException
import org.austral.librawallet.account.exceptions.NotFoundException
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.account.repository.TopUpOrderRepository
import org.austral.librawallet.account.repository.TransactionRepository
import org.austral.librawallet.shared.constants.ErrorMessages
import org.austral.librawallet.shared.formatters.formattedDoubleToCents
import org.springframework.stereotype.Service

@Service
class TopUpService(
    private val accountRepository: AccountRepository,
    private val topUpOrderRepository: TopUpOrderRepository,
    private val transactionRepository: TransactionRepository,
) {

    fun topUp(request: TopUpRequest, jwtUserId: String): TopUpResponse {
        if (request.amount <= 0) {
            throw BadRequestException(ErrorMessages.INVALID_AMOUNT)
        }

        val amountInCents = formattedDoubleToCents(request.amount)

        val account = when (request.identifierType) {
            IdentifierType.CVU -> accountRepository.findByCvu(request.toIdentifier)
            IdentifierType.ALIAS -> accountRepository.findByAlias(request.toIdentifier)
        } ?: throw NotFoundException(ErrorMessages.ACCOUNT_NOT_FOUND)

        // Create top-up order
        topUpOrderRepository.save(
            TopUpOrder(
                account = account,
                amount = amountInCents,
            ),
        )

        // Update account balance immediately
        account.balance += amountInCents
        accountRepository.save(account)

        // Record transaction
        val tx = Transaction(
            account = account,
            otherAccount = account,
            type = TransactionType.INCOME,
            amount = amountInCents,
        )
        transactionRepository.save(tx)

        // Return response
        return TopUpResponse(
            identifier = request.toIdentifier,
            amount = request.amount,
        )
    }
}
