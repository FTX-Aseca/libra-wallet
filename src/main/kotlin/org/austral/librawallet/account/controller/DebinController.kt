package org.austral.librawallet.account.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
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
import io.swagger.v3.oas.annotations.parameters.RequestBody as OpenApiRequestBody

/**
 * Controller to simulate DEBIN operations.
 */
@RestController
@RequestMapping("/api/debin")
@Tag(name = "DEBIN", description = "DEBIN (Debito Inmediato) operations for direct debit transactions")
class DebinController(
    private val debinService: DebinService,
) {

    @PostMapping("/request")
    @Operation(
        summary = "Request DEBIN",
        description = "Initiates a DEBIN (immediate debit) request to another account",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "201",
                description = "DEBIN request created successfully",
                content = [Content(schema = Schema(implementation = DebinResponse::class))],
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid DEBIN request",
            ),
            ApiResponse(
                responseCode = "403",
                description = "Access denied",
            ),
        ],
    )
    @SecurityRequirement(name = "bearerAuth")
    fun requestDebin(
        @OpenApiRequestBody(
            description = "DEBIN request details",
            required = true,
            content = [Content(schema = Schema(implementation = DebinRequestDto::class))],
        )
        @RequestBody request: DebinRequestDto,
        @Parameter(hidden = true)
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<DebinResponse> {
        val response = debinService.requestDebin(request, jwt.subject)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
}
