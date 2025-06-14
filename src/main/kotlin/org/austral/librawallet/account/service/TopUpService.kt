package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.IdentifierType
import org.austral.librawallet.account.dto.topup.TopUpRequest
import org.austral.librawallet.account.dto.topup.TopUpResponse
import org.austral.librawallet.account.entity.TopUpOrder
import org.austral.librawallet.account.entity.Transaction
import org.austral.librawallet.account.entity.TransactionType
import org.austral.librawallet.account.exceptions.BadRequestException
import org.austral.librawallet.account.exceptions.ForbiddenException
import org.austral.librawallet.account.exceptions.NotFoundException
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.account.repository.TopUpOrderRepository
import org.austral.librawallet.account.repository.TransactionRepository
import org.austral.librawallet.shared.constants.ErrorMessages
import org.austral.librawallet.shared.formatters.formattedDoubleToCents
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class TopUpService(
    private val accountRepository: AccountRepository,
    private val topUpOrderRepository: TopUpOrderRepository,
    private val transactionRepository: TransactionRepository,
    @Qualifier("topUpIntegrationServiceImpl") private val topUpIntegrationService: TopUpIntegrationService,
) {

    fun topUp(request: TopUpRequest, jwtUserId: String): TopUpResponse {
        if (request.amount <= 0) {
            throw BadRequestException(ErrorMessages.INVALID_AMOUNT)
        }

        val userIdLong = jwtUserId.toLongOrNull()
            ?: throw ForbiddenException(ErrorMessages.INVALID_USER_ID_FORMAT)
        val account = accountRepository.findByUserId(userIdLong)
            ?: throw NotFoundException(ErrorMessages.ACCOUNT_NOT_FOUND)
        val amountInCents = formattedDoubleToCents(request.amount)
        val success = topUpIntegrationService.performTopUp(
            IdentifierType.CVU,
            request.fromIdentifier,
            request.amount.toLong(),
        )
        if (!success) throw BadRequestException(ErrorMessages.INVALID_CALLBACK_REQUEST)

        val response = TopUpResponse(
            identifier = request.fromIdentifier,
            amount = request.amount.toDouble(),
        )

        topUpOrderRepository.save(
            TopUpOrder(
                account = account,
                amount = amountInCents,
            ),
        )
        // Update account balance immediately
        account.balance = account.balance + amountInCents
        accountRepository.save(account)
        // Record transaction
        val tx = Transaction(
            account = account,
            otherAccount = account,
            type = TransactionType.INCOME,
            amount = amountInCents,
        )
        transactionRepository.save(tx)
        // Return new balance and identifier
        return response
    }
}
