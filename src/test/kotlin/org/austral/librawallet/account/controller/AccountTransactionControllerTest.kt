package org.austral.librawallet.account.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.austral.librawallet.account.dto.transaction.TransactionRequest
import org.austral.librawallet.account.dto.transaction.TransactionResponse
import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.account.entity.Transaction
import org.austral.librawallet.account.entity.TransactionType
import org.austral.librawallet.account.service.AccountTransactionService
import org.austral.librawallet.auth.entity.User
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.austral.librawallet.auth.config.WithMockJwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.austral.librawallet.auth.config.SecurityConfig
import java.time.LocalDateTime
import org.austral.librawallet.account.exceptions.BadRequestException
import org.austral.librawallet.shared.constants.ErrorMessages

@WebMvcTest(AccountTransactionController::class)
@Import(SecurityConfig::class)
class AccountTransactionControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var accountTransactionService: AccountTransactionService

    @Test
    @WithMockJwt(subject = "1")
    fun `getAccountTransactions should return 200 when successful`() {
        // Given
        val accountId = 1L
        val transactions = listOf(
            TransactionResponse(
                type = TransactionType.EXPENSE,
                amount = 50.0,
                timestamp = LocalDateTime.now(),
                description = "Test transaction"
            )
        )
        whenever(accountTransactionService.getAccountTransactions(accountId, "1")).thenReturn(transactions)

        // When/Then
        mockMvc.perform(
            get("/api/accounts/$accountId/transactions")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].type").value("EXPENSE"))
            .andExpect(jsonPath("$[0].amount").value(50.0))
            .andExpect(jsonPath("$[0].description").value("Test transaction"))
    }

    @Test
    @WithMockJwt(subject = "1")
    fun `createTransaction should return 201 when successful`() {
        // Given
        val accountId = 1L
        val request = TransactionRequest(
            type = TransactionType.EXPENSE,
            amount = 50.0,
            description = "Test transaction"
        )
        val response = TransactionResponse(
            type = TransactionType.EXPENSE,
            amount = 50.0,
            timestamp = LocalDateTime.now(),
            description = "Test transaction"
        )
        whenever(accountTransactionService.createTransaction(accountId, request, "1")).thenReturn(response)

        // When/Then
        mockMvc.perform(
            post("/api/accounts/$accountId/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.type").value("EXPENSE"))
            .andExpect(jsonPath("$.amount").value(50.0))
            .andExpect(jsonPath("$.description").value("Test transaction"))
    }

    @Test
    @WithMockJwt(subject = "1")
    fun `createTransaction should return 400 when request is invalid`() {
        // Given
        val accountId = 1L
        val request = TransactionRequest(
            type = TransactionType.EXPENSE,
            amount = -50.0,
            description = "Test transaction"
        )
        whenever(accountTransactionService.createTransaction(accountId, request, "1"))
            .thenThrow(BadRequestException(ErrorMessages.INVALID_AMOUNT))

        // When/Then
        mockMvc.perform(
            post("/api/accounts/$accountId/transactions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(ErrorMessages.INVALID_AMOUNT))
    }
} 