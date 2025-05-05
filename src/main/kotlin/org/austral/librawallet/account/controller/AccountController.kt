package org.austral.librawallet.account.controller

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
class AccountController(
    private val accountService: AccountService,
) {

    @GetMapping("/{accountId}/balance")
    fun getAccountBalance(
        @PathVariable accountId: Long,
        @AuthenticationPrincipal jwt: Jwt
    ): ResponseEntity<BalanceResponse> {
        val balance = accountService.getBalance(accountId, jwt.subject)
        return ResponseEntity.ok(BalanceResponse(balance))
    }
}