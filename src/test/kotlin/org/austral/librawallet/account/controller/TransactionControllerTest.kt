package org.austral.librawallet.account.controller

import org.austral.librawallet.account.dto.transaction.TransactionResponse
import org.austral.librawallet.account.entity.TransactionType
import org.austral.librawallet.account.service.TransferService
import org.austral.librawallet.auth.config.SecurityConfig
import org.austral.librawallet.auth.config.WithMockJwt
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.LocalDateTime

@WebMvcTest(TransactionController::class)
@Import(SecurityConfig::class)
class TransactionControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockitoBean
    private lateinit var transferService: TransferService

    @Test
    @WithMockJwt(subject = "1")
    fun `getTransactions should return 200 when successful`() {
        // Given
        val transactions = listOf(
            TransactionResponse(
                type = TransactionType.EXPENSE,
                amount = 50.0,
                timestamp = LocalDateTime.now(),
                description = "Test transfer",
            ),
        )

        whenever(transferService.getTransactions("1")).thenReturn(transactions)

        // When/Then
        mockMvc.perform(
            get("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].type").value("EXPENSE"))
            .andExpect(jsonPath("$[0].amount").value(50.0))
            .andExpect(jsonPath("$[0].description").value("Test transfer"))
    }

    @Test
    fun `getTransactions should return 401 when not authenticated`() {
        // When/Then
        mockMvc.perform(
            get("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isUnauthorized)
    }
}
