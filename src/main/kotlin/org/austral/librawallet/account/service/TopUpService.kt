package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.topup.TopUpCallbackRequest
import org.austral.librawallet.account.dto.topup.TopUpRequest
import org.austral.librawallet.account.dto.topup.TopUpResponse
import org.austral.librawallet.account.entity.TopUpOrder
import org.austral.librawallet.account.entity.TopUpStatus
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
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TopUpService(
    private val accountRepository: AccountRepository,
    private val topUpOrderRepository: TopUpOrderRepository,
    private val transactionRepository: TransactionRepository,
) {

    fun topUp(request: TopUpRequest, jwtUserId: String): TopUpResponse {
        val userIdLong = jwtUserId.toLongOrNull()
            ?: throw ForbiddenException(ErrorMessages.INVALID_USER_ID_FORMAT)
        val account = accountRepository.findByUserId(userIdLong)
            ?: throw NotFoundException(ErrorMessages.ACCOUNT_NOT_FOUND)
        val amountInCents = formattedDoubleToCents(request.amount)
        val order = topUpOrderRepository.save(
            TopUpOrder(
                account = account,
                amount = amountInCents,
                status = TopUpStatus.PENDING,
            ),
        )
        return TopUpResponse(
            id = order.id!!,
            status = order.status.name,
        )
    }

    @Transactional
    fun handleCallback(callback: TopUpCallbackRequest): TopUpResponse {
        val order = topUpOrderRepository.findById(callback.id)
            .orElseThrow { BadRequestException(ErrorMessages.INVALID_CALLBACK_REQUEST) }
        if (order.status != TopUpStatus.PENDING) {
            throw BadRequestException(ErrorMessages.INVALID_CALLBACK_REQUEST)
        }
        val account = order.account
        account.balance = account.balance + order.amount
        accountRepository.save(account)
        val tx = Transaction(
            account = account,
            otherAccount = account,
            type = TransactionType.INCOME,
            amount = order.amount,
        )
        transactionRepository.save(tx)
        order.status = TopUpStatus.COMPLETED
        topUpOrderRepository.save(order)
        return TopUpResponse(
            id = order.id!!,
            status = order.status.name,
        )
    }
}
