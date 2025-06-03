package org.austral.librawallet.account.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.austral.librawallet.account.dto.AccountDetails
import org.austral.librawallet.account.dto.BalanceResponse
import org.austral.librawallet.account.service.AccountService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Account management and balance operations")
@SecurityRequirement(name = "bearerAuth")
class AccountController(
    private val accountService: AccountService,
) {

    @GetMapping("/{accountId}/balance")
    @Operation(
        summary = "Get account balance",
        description = "Retrieves the current balance for a specific account",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Balance retrieved successfully",
                content = [Content(schema = Schema(implementation = BalanceResponse::class))],
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
    fun getAccountBalance(
        @Parameter(description = "Account ID", required = true)
        @PathVariable accountId: Long,
        @Parameter(hidden = true)
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<BalanceResponse> {
        val balance = accountService.getBalance(accountId, jwt.subject)
        return ResponseEntity.ok(BalanceResponse(balance))
    }

    @GetMapping("/{accountId}")
    @Operation(
        summary = "Get account details",
        description = "Retrieves detailed information about a specific account",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Account details retrieved successfully",
                content = [Content(schema = Schema(implementation = AccountDetails::class))],
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
    fun getAccountDetails(
        @Parameter(description = "Account ID", required = true)
        @PathVariable accountId: Long,
        @Parameter(hidden = true)
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<AccountDetails> {
        val accountDetails = accountService.getAccountDetails(accountId, jwt.subject)
        return ResponseEntity.ok(accountDetails)
    }
}
