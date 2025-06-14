package org.austral.librawallet.account.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
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
import io.swagger.v3.oas.annotations.parameters.RequestBody as OpenApiRequestBody

/**
 * Controller to simulate external top-up operations.
 */
@RestController
@RequestMapping("/api/topup")
@Tag(name = "Top-up", description = "Account top-up operations from external sources")
class TopUpController(
    private val topUpService: TopUpService,
) {

    @PostMapping
    @Operation(
        summary = "Top-up account",
        description = "Initiates a top-up operation to add funds to an account from external sources",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "Top-up request created successfully",
                content = [Content(schema = Schema(implementation = TopUpResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid top-up request",
            ),
            ApiResponse(
                responseCode = "403",
                description = "Access denied",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    fun topUp(
        @OpenApiRequestBody(
            description = "Top-up request details",
            required = true,
            content = [Content(schema = Schema(implementation = TopUpRequest::class))],
        )
        @RequestBody request: TopUpRequest,
        @Parameter(hidden = true)
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<TopUpResponse> {
        val response = topUpService.topUp(request, jwt.subject)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/callback")
    @Operation(
        summary = "Top-up callback",
        description = "Handles top-up callback responses from external payment systems",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Callback processed successfully",
                content = [Content(schema = Schema(implementation = TopUpResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid callback request",
            ),
        ],
    )
    fun callback(
        @OpenApiRequestBody(
            description = "Top-up callback request",
            required = true,
            content = [Content(schema = Schema(implementation = TopUpCallbackRequest::class))],
        )
        @RequestBody request: TopUpCallbackRequest,
    ): ResponseEntity<TopUpResponse> {
        val response = topUpService.handleCallback(request)
        return ResponseEntity.ok(response)
    }
}
