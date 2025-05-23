package org.austral.librawallet.account.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.austral.librawallet.account.dto.IdentifierType
import org.austral.librawallet.account.dto.transfer.TransferRequest
import org.austral.librawallet.account.dto.transfer.TransferResponse
import org.austral.librawallet.account.service.TransferService
import org.austral.librawallet.auth.config.SecurityConfig
import org.austral.librawallet.auth.config.WithMockJwt
import org.austral.librawallet.account.exceptions.BadRequestException
import org.austral.librawallet.shared.constants.ErrorMessages
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@WebMvcTest(TransferController::class)
@Import(SecurityConfig::class)
class TransferControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var transferService: TransferService

    @Test
    @WithMockJwt(subject = "1")
    fun `transfer should return 201 when successful`() {
        // Given
        val request = TransferRequest(
            toIdentifier = "receiver-cvu",
            identifierType = IdentifierType.CVU,
            amount = 50.0
        )
        val response = TransferResponse(
            fromBalance = 50.0,
            toBalance = 100.0
        )
        whenever(transferService.transfer(request, "1")).thenReturn(response)

        // When/Then
        mockMvc.perform(
            post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.fromBalance").value(50.0))
            .andExpect(jsonPath("$.toBalance").value(100.0))
    }

    @Test
    @WithMockJwt(subject = "1")
    fun `transfer should return 400 when request is invalid`() {
        // Given
        val request = TransferRequest(
            toIdentifier = "receiver-cvu",
            identifierType = IdentifierType.CVU,
            amount = -50.0
        )

        whenever(transferService.transfer(request, "1")).thenThrow(BadRequestException(ErrorMessages.INVALID_AMOUNT))

        // When/Then
        mockMvc.perform(
            post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(ErrorMessages.INVALID_AMOUNT))
    }

    @Test
    fun `transfer should return 401 when not authenticated`() {
        // Given
        val request = TransferRequest(
            toIdentifier = "receiver-cvu",
            identifierType = IdentifierType.CVU,
            amount = 50.0
        )

        // When/Then
        mockMvc.perform(
            post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }
} 