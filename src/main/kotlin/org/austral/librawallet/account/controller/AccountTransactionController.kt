package org.austral.librawallet.account.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Account Transactions", description = "Transaction management for specific accounts")
@SecurityRequirement(name = "bearerAuth")
class AccountTransactionController(
    private val accountTransactionService: AccountTransactionService,
) {

    @GetMapping
    @Operation(
        summary = "Get account transactions",
        description = "Retrieves all transactions for a specific account",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Transactions retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<TransactionResponse>::class))],
            ),
            ApiResponse(
                responseCode = "403",
                description = "Access denied - user doesn't own this account",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Account not found",
            ),
        ],
    )
    fun getAccountTransactions(
        @Parameter(description = "Account ID", required = true)
        @PathVariable accountId: Long,
        @Parameter(hidden = true)
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<List<TransactionResponse>> {
        val jwtUserId = jwt.subject
        val transactions = accountTransactionService.getAccountTransactions(accountId, jwtUserId)
        return ResponseEntity.ok(transactions)
    }

    @PostMapping
    @Operation(
        summary = "Create transaction",
        description = "Creates a new transaction for a specific account",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Transaction created successfully",
                content = [Content(schema = Schema(implementation = TransactionResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid transaction request",
            ),
            ApiResponse(
                responseCode = "403",
                description = "Access denied - user doesn't own this account",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Account not found",
            ),
        ],
    )
    fun createTransaction(
        @Parameter(description = "Account ID", required = true)
        @PathVariable accountId: Long,
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Transaction request details",
            required = true,
            content = [Content(schema = Schema(implementation = TransactionRequest::class))],
        )
        @RequestBody request: TransactionRequest,
        @Parameter(hidden = true)
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<TransactionResponse> {
        val jwtUserId = jwt.subject
        val transaction = accountTransactionService.createTransaction(accountId, request, jwtUserId)
        return ResponseEntity.status(HttpStatus.CREATED).body(transaction)
    }
}
