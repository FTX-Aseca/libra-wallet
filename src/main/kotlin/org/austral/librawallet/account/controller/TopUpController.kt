package org.austral.librawallet.account.controller

import org.austral.librawallet.account.dto.topup.TopUpCallbackRequest
import org.austral.librawallet.account.dto.topup.TopUpRequest
import org.austral.librawallet.account.dto.topup.TopUpResponse
import org.austral.librawallet.account.service.TopUpService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller to simulate external top-up operations.
 */
@RestController
@RequestMapping("/api/topup")
class TopUpController(
    private val topUpService: TopUpService,
) {

    @PostMapping
    fun topUp(
        @RequestBody request: TopUpRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<TopUpResponse> {
        val response = topUpService.topUp(request, jwt.subject)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/callback")
    fun callback(
        @RequestBody request: TopUpCallbackRequest,
    ): ResponseEntity<TopUpResponse> {
        val response = topUpService.handleCallback(request)
        return ResponseEntity.ok(response)
    }
}
