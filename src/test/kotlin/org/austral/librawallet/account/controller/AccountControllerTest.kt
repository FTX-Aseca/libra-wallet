package org.austral.librawallet.account.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.austral.librawallet.account.dto.BalanceResponse
import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.auth.entity.User
import org.austral.librawallet.account.service.AccountService
import org.austral.librawallet.account.exceptions.ForbiddenException
import org.austral.librawallet.account.exceptions.NotFoundException
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.austral.librawallet.auth.config.SecurityConfig

@WebMvcTest(AccountController::class)
@Import(SecurityConfig::class)
class AccountControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var accountService: AccountService

    @Test
    @WithMockJwt(subject = "1")
    fun `getAccountBalance should return 200 when successful`() {
        // Given
        val user = User(id = 1L, email = "test@example.com", password = "password")
        val account = Account(
            id = 1L,
            user = user,
            balance = 1000L // cents
        )
        whenever(accountService.getBalance(account.id!!, "1")).thenReturn(10.0)

        // When/Then
        mockMvc.perform(
            get("/api/accounts/${account.id}/balance")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(10.0))
    }

    @Test
    @WithMockJwt(subject = "1")
    fun `getAccountBalance should return 404 when account not found`() {
        // Given
        val accountId = 1L
        whenever(accountService.getBalance(accountId, "1")).thenThrow(NotFoundException("Account not found"))

        // When/Then
        mockMvc.perform(
            get("/api/accounts/$accountId/balance")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
    }

    @Test
    @WithMockJwt(subject = "2")
    fun `getAccountBalance should return 403 when account belongs to different user`() {
        // Given
        val accountId = 1L
        whenever(accountService.getBalance(accountId, "2")).thenThrow(ForbiddenException())

        // When/Then
        mockMvc.perform(
            get("/api/accounts/$accountId/balance")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isForbidden)
    }
} 