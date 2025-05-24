package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.debin.DebinCallbackRequest
import org.austral.librawallet.account.dto.debin.DebinRequestDto
import org.austral.librawallet.account.dto.debin.DebinResponse
import org.austral.librawallet.account.entity.DebinRequest
import org.austral.librawallet.account.entity.DebinStatus
import org.austral.librawallet.account.entity.Transaction
import org.austral.librawallet.account.entity.TransactionType
import org.austral.librawallet.account.exceptions.BadRequestException
import org.austral.librawallet.account.exceptions.ForbiddenException
import org.austral.librawallet.account.exceptions.NotFoundException
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.account.repository.DebinRequestRepository
import org.austral.librawallet.account.repository.TransactionRepository
import org.austral.librawallet.shared.constants.ErrorMessages
import org.austral.librawallet.shared.formatters.formattedDoubleToCents
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DebinService(
    private val accountRepository: AccountRepository,
    private val debinRequestRepository: DebinRequestRepository,
    private val transactionRepository: TransactionRepository,
) {

    fun requestDebin(request: DebinRequestDto, jwtUserId: String): DebinResponse {
        if (request.amount <= 0) {
            throw BadRequestException(ErrorMessages.INVALID_AMOUNT)
        }
        val userIdLong = jwtUserId.toLongOrNull()
            ?: throw ForbiddenException(ErrorMessages.INVALID_USER_ID_FORMAT)
        val account = accountRepository.findByUserId(userIdLong)
            ?: throw NotFoundException(ErrorMessages.ACCOUNT_NOT_FOUND)
        val amountInCents = formattedDoubleToCents(request.amount)
        val debin = debinRequestRepository.save(
            DebinRequest(
                account = account,
                amount = amountInCents,
                status = DebinStatus.PENDING,
            ),
        )
        return DebinResponse(
            id = debin.id!!,
            amount = request.amount,
            status = debin.status.name,
        )
    }

    @Transactional
    fun handleCallback(callback: DebinCallbackRequest): DebinResponse {
        if (callback.id <= 0) {
            throw BadRequestException(ErrorMessages.INVALID_CALLBACK_REQUEST)
        }
        val debin = debinRequestRepository.findById(callback.id)
            .orElseThrow { BadRequestException(ErrorMessages.INVALID_CALLBACK_REQUEST) }
        if (debin.status != DebinStatus.PENDING) {
            throw BadRequestException(ErrorMessages.INVALID_CALLBACK_REQUEST)
        }
        // credit account
        val account = debin.account
        account.balance = account.balance + debin.amount
        accountRepository.save(account)
        // record transaction
        val tx = Transaction(
            account = account,
            otherAccount = account,
            type = TransactionType.INCOME,
            amount = debin.amount,
        )
        transactionRepository.save(tx)
        // update debin status
        debin.status = DebinStatus.COMPLETED
        debinRequestRepository.save(debin)
        return DebinResponse(
            id = debin.id!!,
            amount = debin.amount / 100.0, // Convert cents to dollars
            status = debin.status.name,
        )
    }
}
