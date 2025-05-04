package org.austral.librawallet.account.controller

import org.austral.librawallet.account.dto.BalanceResponse
import org.austral.librawallet.account.service.AccountService
import org.austral.librawallet.auth.util.JwtUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/accounts")
class AccountController(
    private val accountService: AccountService,
    private val jwtUtil: JwtUtil,
) {

    @GetMapping("/{accountId}/balance")
    fun getAccountBalance(
        @PathVariable accountId: Long,
        @RequestHeader("Authorization") authorization: String,
    ): ResponseEntity<BalanceResponse> {
        val token = authorization.removePrefix("Bearer ").trim()
        val userId = jwtUtil.validateTokenAndGetSubject(token)

        val balance = accountService.getBalance(accountId, userId)
        return ResponseEntity.ok(BalanceResponse(balance))
    }
}