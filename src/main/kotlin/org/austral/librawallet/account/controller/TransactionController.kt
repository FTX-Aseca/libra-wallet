package org.austral.librawallet.account.controller

import org.austral.librawallet.account.dto.transaction.TransactionResponse
import org.austral.librawallet.account.service.TransferService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller to retrieve transaction history for authenticated user.
 */
@RestController
@RequestMapping("/api/transactions")
class TransactionController(
    private val transferService: TransferService,
) {

    @GetMapping
    fun getTransactions(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<List<TransactionResponse>> {
        val jwtUserId = jwt.subject
        val transactions = transferService.getTransactions(jwtUserId)
        return ResponseEntity.ok(transactions)
    }
}
