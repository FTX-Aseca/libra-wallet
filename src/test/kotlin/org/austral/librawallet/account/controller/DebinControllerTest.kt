package org.austral.librawallet.account.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.austral.librawallet.account.dto.IdentifierType
import org.austral.librawallet.account.dto.debin.DebinRequestDto
import org.austral.librawallet.account.dto.debin.DebinResponse
import org.austral.librawallet.account.exceptions.BadRequestException
import org.austral.librawallet.account.service.DebinService
import org.austral.librawallet.auth.config.SecurityConfig
import org.austral.librawallet.auth.config.WithMockJwt
import org.austral.librawallet.shared.constants.ErrorMessages
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(DebinController::class)
@Import(SecurityConfig::class)
class DebinControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var debinService: DebinService

    @Test
    @WithMockJwt(subject = "1")
    fun `performing DEBIN should return 200 when successful`() {
        // Given
        val request = DebinRequestDto(
            amount = 50.0,
            identifierType = IdentifierType.CVU,
            fromIdentifier = "0".repeat(22),
        )
        val response = DebinResponse(
            identifier = "",
            amount = 50.0,
        )
        whenever(debinService.requestDebin(request, "1")).thenReturn(response)

        // When/Then
        mockMvc.perform(
            post("/api/debin/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.amount").value(50.0))
            .andExpect(jsonPath("$.status").value("COMPLETED"))
    }

    @Test
    @WithMockJwt(subject = "1")
    fun `requestDebin should return 400 when request is invalid`() {
        // Given
        val request = DebinRequestDto(
            amount = -50.0,
            identifierType = IdentifierType.ALIAS,
            fromIdentifier = "Alice",
        )
        whenever(debinService.requestDebin(request, "1")).thenThrow(BadRequestException(ErrorMessages.INVALID_AMOUNT))

        // When/Then
        mockMvc.perform(
            post("/api/debin/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(ErrorMessages.INVALID_AMOUNT))
    }
}
