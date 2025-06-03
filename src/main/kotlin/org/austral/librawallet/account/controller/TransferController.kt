package org.austral.librawallet.account.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(name = "Transfers", description = "Peer-to-peer money transfer operations")
@SecurityRequirement(name = "bearerAuth")
class TransferController(
    private val transferService: TransferService,
) {

    @PostMapping
    @Operation(
        summary = "Transfer money",
        description = "Initiates a peer-to-peer money transfer between accounts",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Transfer completed successfully",
                content = [Content(schema = Schema(implementation = TransferResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid transfer request or insufficient funds",
            ),
            ApiResponse(
                responseCode = "403",
                description = "Access denied - user doesn't own the source account",
            ),
            ApiResponse(
                responseCode = "404",
                description = "Source or destination account not found",
            ),
        ],
    )
    fun transfer(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Transfer request details",
            required = true,
            content = [Content(schema = Schema(implementation = TransferRequest::class))],
        )
        @RequestBody request: TransferRequest,
        @Parameter(hidden = true)
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<TransferResponse> {
        val response = transferService.transfer(request, jwt.subject)
        return ResponseEntity.ok(response)
    }
}
