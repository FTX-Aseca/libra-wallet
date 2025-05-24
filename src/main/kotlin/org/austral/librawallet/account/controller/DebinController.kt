package org.austral.librawallet.account.controller

import org.austral.librawallet.account.dto.debin.DebinCallbackRequest
import org.austral.librawallet.account.dto.debin.DebinRequestDto
import org.austral.librawallet.account.dto.debin.DebinResponse
import org.austral.librawallet.account.service.DebinService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Controller to simulate DEBIN operations.
 */
@RestController
@RequestMapping("/api/debin")
class DebinController(
    private val debinService: DebinService,
) {

    @PostMapping("/request")
    fun requestDebin(
        @RequestBody request: DebinRequestDto,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<DebinResponse> {
        val response = debinService.requestDebin(request, jwt.subject)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/callback")
    fun callback(
        @RequestBody callbackRequest: DebinCallbackRequest,
    ): ResponseEntity<DebinResponse> {
        val response = debinService.handleCallback(callbackRequest)
        return ResponseEntity.ok(response)
    }
}
