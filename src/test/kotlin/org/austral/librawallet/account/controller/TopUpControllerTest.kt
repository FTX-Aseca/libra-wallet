package org.austral.librawallet.account.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.austral.librawallet.account.dto.IdentifierType
import org.austral.librawallet.account.dto.topup.TopUpRequest
import org.austral.librawallet.account.dto.topup.TopUpResponse
import org.austral.librawallet.account.exceptions.BadRequestException
import org.austral.librawallet.account.service.TopUpService
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

@WebMvcTest(TopUpController::class)
@Import(SecurityConfig::class)
class TopUpControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockitoBean
    private lateinit var topUpService: TopUpService

    @Test
    @WithMockJwt(subject = "1")
    fun `topUp should return 200 when successful`() {
        val identifier = "0".repeat(22)
        // Given
        val request = TopUpRequest(
            amount = 100.0,
            identifierType = IdentifierType.CVU,
            identifier,
        )

        val response = TopUpResponse(
            identifier = identifier,
            amount = 100.0,
        )

        whenever(topUpService.topUp(request, "1")).thenReturn(response)

        // When/Then
        mockMvc.perform(
            post("/api/topup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andExpect(status().isOk)
    }

    @Test
    @WithMockJwt(subject = "1")
    fun `topUp should return 400 when request is invalid`() {
        // Given
        val request = TopUpRequest(
            amount = -100.0,
            identifierType = IdentifierType.CVU,
            toIdentifier = "0".repeat(22),
        )

        whenever(topUpService.topUp(request, "1")).thenThrow(BadRequestException(ErrorMessages.INVALID_AMOUNT))

        // When/Then
        mockMvc.perform(
            post("/api/topup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(ErrorMessages.INVALID_AMOUNT))
    }
}
