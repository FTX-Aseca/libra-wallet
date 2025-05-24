package org.austral.librawallet.account.controller

import org.austral.librawallet.account.dto.transfer.TransferRequest
import org.austral.librawallet.account.dto.transfer.TransferResponse
import org.austral.librawallet.account.service.TransferService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller to handle peer-to-peer money transfers.
 */
@RestController
@RequestMapping("/api/transfers")
class TransferController(
    private val transferService: TransferService,
) {

    @PostMapping
    fun transfer(
        @RequestBody request: TransferRequest,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<TransferResponse> {
        val response = transferService.transfer(request, jwt.subject)
        return ResponseEntity.ok(response)
    }
}
