package org.austral.librawallet.account.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.austral.librawallet.account.dto.debin.DebinRequestDto
import org.austral.librawallet.account.dto.debin.DebinResponse
import org.austral.librawallet.account.dto.debin.DebinCallbackRequest
import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.account.service.DebinService
import org.austral.librawallet.auth.entity.User
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
import org.austral.librawallet.auth.config.SecurityConfig

@WebMvcTest(DebinController::class)
@Import(SecurityConfig::class)
class DebinControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var debinService: DebinService

    @Test
    @WithMockJwt(subject = "1")
    fun `requestDebin should return 201 when successful`() {
        // Given
        val request = DebinRequestDto(
            amount = 50.0
        )
        val response = DebinResponse(
            id = 1L,
            amount = 50.0,
            status = "PENDING"
        )
        whenever(debinService.requestDebin(request, "1")).thenReturn(response)

        // When/Then
        mockMvc.perform(
            post("/api/debin/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.amount").value(50.0))
            .andExpect(jsonPath("$.status").value("PENDING"))
    }

    @Test
    @WithMockJwt(subject = "1")
    fun `requestDebin should return 400 when request is invalid`() {
        // Given
        val request = DebinRequestDto(
            amount = -50.0
        )
        whenever(debinService.requestDebin(request, "1")).thenThrow(BadRequestException(ErrorMessages.INVALID_AMOUNT))

        // When/Then
        mockMvc.perform(
            post("/api/debin/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(ErrorMessages.INVALID_AMOUNT))
    }

    @Test
    @WithMockJwt(subject = "1")
    fun `callback should return 200 when successful`() {
        // Given
        val request = DebinCallbackRequest(
            id = 1L
        )
        val response = DebinResponse(
            id = 1L,
            amount = 50.0,
            status = "COMPLETED"
        )
        whenever(debinService.handleCallback(request)).thenReturn(response)

        // When/Then
        mockMvc.perform(
            post("/api/debin/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.amount").value(50.0))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
    }

    @Test
    @WithMockJwt(subject = "1")
    fun `callback should return 400 when request is invalid`() {
        // Given
        val request = DebinCallbackRequest(
            id = -1L
        )
        whenever(debinService.handleCallback(request)).thenThrow(BadRequestException(ErrorMessages.INVALID_CALLBACK_REQUEST))

        // When/Then
        mockMvc.perform(
            post("/api/debin/callback")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(ErrorMessages.INVALID_CALLBACK_REQUEST))
    }
} 