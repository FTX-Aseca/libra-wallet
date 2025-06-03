package org.austral.librawallet.account.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Transactions", description = "General transaction history operations")
@SecurityRequirement(name = "bearerAuth")
class TransactionController(
    private val transferService: TransferService,
) {

    @GetMapping
    @Operation(
        summary = "Get user transactions",
        description = "Retrieves all transactions for the authenticated user across all their accounts",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Transactions retrieved successfully",
                content = [Content(schema = Schema(implementation = Array<TransactionResponse>::class))],
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - invalid or missing JWT token",
            ),
        ],
    )
    fun getTransactions(
        @Parameter(hidden = true)
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<List<TransactionResponse>> {
        val jwtUserId = jwt.subject
        val transactions = transferService.getTransactions(jwtUserId)
        return ResponseEntity.ok(transactions)
    }
}
