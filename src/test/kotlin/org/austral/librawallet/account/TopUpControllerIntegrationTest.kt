package org.austral.librawallet.account

import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.account.entity.TopUpOrder
import org.austral.librawallet.account.entity.TopUpStatus
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.account.repository.TopUpOrderRepository
import org.austral.librawallet.auth.repository.UserRepository
import org.austral.librawallet.auth.util.JwtUtil
import org.austral.librawallet.shared.constants.ErrorMessages
import org.austral.librawallet.shared.formatters.formattedDoubleToCents
import org.austral.librawallet.util.DatabaseInitializationService
import org.austral.librawallet.util.UserTestUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TopUpControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var databaseInitializationService: DatabaseInitializationService

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var jwtUtils: JwtUtil

    @Autowired
    private lateinit var topUpOrderRepository: TopUpOrderRepository

    @Autowired
    private lateinit var userTestUtils: UserTestUtils

    @BeforeEach
    fun setup() {
        databaseInitializationService.clean()
    }

    @Test
    fun `AC1 top-up returns 201 with order ID and PENDING status`() {
        val (user, token) = userTestUtils.createUserAndToken("user@example.com", "Pass1!")
        // ensure account exists
        accountRepository.save(
            Account(user = user, balance = 0L),
        )
        val amount = 100.00
        val requestBody = """
            { "amount": $amount }
        """.trimIndent()

        mockMvc.perform(
            post("/api/topup")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.status").value("PENDING"))
    }

    @Test
    fun `AC2 top-up callback valid transitions to COMPLETED and credits user`() {
        val (user, token) = userTestUtils.createUserAndToken("user2@example.com", "Pass2!")
        val account = accountRepository.save(Account(user = user, balance = 0L))
        val amount = 50.0
        // create a pending top-up order
        val topUp = topUpOrderRepository.save(
            TopUpOrder(
                account = account,
                amount = formattedDoubleToCents(amount),
                status = TopUpStatus.PENDING,
            ),
        )
        val callbackBody = """
            { "id": ${topUp.id} }
        """.trimIndent()

        mockMvc.perform(
            post("/api/topup/callback")
                .header("Authorization", token)
                .header("X-Signature", "valid-signature")
                .contentType(MediaType.APPLICATION_JSON)
                .content(callbackBody),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(TopUpStatus.COMPLETED.name))

        // verify transaction history
        mockMvc.perform(
            get("/api/transactions")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].type").value("INCOME"))
            .andExpect(jsonPath("$[0].amount").value(amount))
    }

    @Test
    fun `AC3 invalid top-up callback returns 400`() {
        val (user, token) = userTestUtils.createUserAndToken("user3@example.com", "Pass3!")
        val account = accountRepository.save(Account(user = user, balance = 0L))
        val amount = 25.0
        // create a pending top-up order
        val topUp = topUpOrderRepository.save(
            TopUpOrder(
                account = account,
                amount = formattedDoubleToCents(amount),
                status = TopUpStatus.PENDING,
            ),
        )
        val callbackBody = """
            { "id": ${topUp.id} }
        """.trimIndent()

        mockMvc.perform(
            post("/api/topup/callback")
                .header("Authorization", token)
                .header("X-Signature", "invalid-signature")
                .contentType(MediaType.APPLICATION_JSON)
                .content(callbackBody),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(ErrorMessages.INVALID_SIGNATURE))
    }
}
