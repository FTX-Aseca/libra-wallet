package org.austral.librawallet.account.controller

import org.austral.librawallet.account.dto.transaction.TransactionRequest
import org.austral.librawallet.account.dto.transaction.TransactionResponse
import org.austral.librawallet.account.service.AccountTransactionService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller to manage transactions for specific accounts.
 */
@RestController
@RequestMapping("/api/accounts/{accountId}/transactions")
class AccountTransactionController(
    private val accountTransactionService: AccountTransactionService,
) {

    @GetMapping
    fun getAccountTransactions(
        @PathVariable accountId: Long,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<List<TransactionResponse>> {
        val jwtUserId = jwt.subject
        val transactions = accountTransactionService.getAccountTransactions(accountId, jwtUserId)
        return ResponseEntity.ok(transactions)
    }

    @PostMapping
    fun createTransaction(
        @PathVariable accountId: Long,
        @RequestBody request: TransactionRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<TransactionResponse> {
        val jwtUserId = jwt.subject
        val transaction = accountTransactionService.createTransaction(accountId, request, jwtUserId)
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction)
    }
}
