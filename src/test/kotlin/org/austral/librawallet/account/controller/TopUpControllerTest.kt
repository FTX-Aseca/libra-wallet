package org.austral.librawallet.account.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.austral.librawallet.account.dto.topup.TopUpRequest
import org.austral.librawallet.account.dto.topup.TopUpResponse
import org.austral.librawallet.account.dto.topup.TopUpCallbackRequest
import org.austral.librawallet.account.service.TopUpService
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

@WebMvcTest(TopUpController::class)
@Import(SecurityConfig::class)
class TopUpControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var topUpService: TopUpService

    @Test
    @WithMockJwt(subject = "1")
    fun `topUp should return 201 when successful`() {
        // Given
        val request = TopUpRequest(
            amount = 100.0
        )

        val response = TopUpResponse(
            id = 1L,
            status = "PENDING"
        )

        whenever(topUpService.topUp(request, "1")).thenReturn(response)

        // When/Then
        mockMvc.perform(
            post("/api/topup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("PENDING"))
    }

    @Test
    @WithMockJwt(subject = "1")
    fun `topUp should return 400 when request is invalid`() {
        // Given
        val request = TopUpRequest(
            amount = -100.0
        )

        whenever(topUpService.topUp(request, "1")).thenThrow(BadRequestException(ErrorMessages.INVALID_AMOUNT))

        // When/Then
        mockMvc.perform(
            post("/api/topup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(ErrorMessages.INVALID_AMOUNT))
    }

    @Test
    fun `topUp should return 401 when not authenticated`() {
        // Given
        val request = TopUpRequest(
            amount = 100.0
        )

        // When/Then
        mockMvc.perform(
            post("/api/topup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `callback should return 200 when successful`() {
        // Given
        val request = TopUpCallbackRequest(
            id = 1L
        )

        val response = TopUpResponse(
            id = request.id,
            status = "COMPLETED"
        )

        whenever(topUpService.handleCallback(request)).thenReturn(response)

        // When/Then
        mockMvc.perform(
            post("/api/topup/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
    }

    @Test
    fun `callback should return 400 when request is invalid`() {
        // Given
        val request = TopUpCallbackRequest(
            id = -1L
        )

        whenever(topUpService.handleCallback(request)).thenThrow(BadRequestException(ErrorMessages.INVALID_CALLBACK_REQUEST))

        // When/Then
        mockMvc.perform(
            post("/api/topup/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(ErrorMessages.INVALID_CALLBACK_REQUEST))
    }
} 