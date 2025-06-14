package org.austral.librawallet.account.service

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
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service

@Service
class DebinService(
    private val accountRepository: AccountRepository,
    private val debinRequestRepository: DebinRequestRepository,
    private val transactionRepository: TransactionRepository,
    @Qualifier("debinIntegrationServiceImpl") private val debinIntegrationService: DebinIntegrationService,
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
        val success = debinIntegrationService.performDebin(
            request.identifierType,
            request.fromIdentifier,
            amountInCents,
        )
        val status = if (success) DebinStatus.COMPLETED else DebinStatus.FAILED
        val debin = debinRequestRepository.save(
            DebinRequest(
                account = account,
                amount = amountInCents,
                identifierType = request.identifierType,
                fromIdentifier = request.fromIdentifier,
                status = status,
            ),
        )
        if (!success) {
            throw BadRequestException(ErrorMessages.DEBIN_REQUEST_FAILED)
        }
        account.balance = account.balance + amountInCents
        accountRepository.save(account)
        val tx = Transaction(
            account = account,
            otherAccount = account,
            type = TransactionType.INCOME,
            amount = amountInCents,
        )
        transactionRepository.save(tx)
        return DebinResponse(
            identifier = request.fromIdentifier,
            amount = account.balance.toDouble(),
        )
    }
}
