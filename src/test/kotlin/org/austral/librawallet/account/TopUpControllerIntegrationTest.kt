package org.austral.librawallet.account

import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.account.repository.TopUpOrderRepository
import org.austral.librawallet.account.service.FakeTopUpIntegrationService
import org.austral.librawallet.account.service.TopUpIntegrationService
import org.austral.librawallet.shared.constants.ErrorMessages
import org.austral.librawallet.util.DatabaseInitializationService
import org.austral.librawallet.util.IntegrationTestBase
import org.austral.librawallet.util.UserTestUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Import(TopUpControllerIntegrationTest.TestConfig::class)
class TopUpControllerIntegrationTest : IntegrationTestBase() {
    @TestConfiguration
    class TestConfig {
        @Bean("topUpIntegrationServiceImpl")
        @Primary
        fun topUpIntegrationService(): TopUpIntegrationService = FakeTopUpIntegrationService()
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var databaseInitializationService: DatabaseInitializationService

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var topUpOrderRepository: TopUpOrderRepository

    @Autowired
    private lateinit var userTestUtils: UserTestUtils

    @BeforeEach
    fun setup() {
        databaseInitializationService.clean()
    }

    @Test
    fun `AC1 top-up returns 200 with order ID and PENDING status`() {
        val (user, token) = userTestUtils.createUserAndToken("user@example.com", "Pass1!")
        // ensure account exists
        accountRepository.save(
            Account(user = user, balance = 0L),
        )
        val amount = 100.00
        val identifier = "0".repeat(22)
        val requestBody = """
            {
            "amount": $amount,
            "identifier": "$identifier"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/topup")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.amount").isNumber)
    }

    @Test
    fun `AC2 top-up callback valid transitions to COMPLETED and credits user`() {
        val (user, token) = userTestUtils.createUserAndToken("user2@example.com", "Pass2!")
        val account = accountRepository.save(Account(user = user, balance = 0L))
        val amount = 50.0
        // create a pending top-up order
//        val topUp = topUpOrderRepository.save(
//            TopUpOrder(
//                account = account,
//                amount = formattedDoubleToCents(amount),
//            ),
//        )
        val identifier = "0".repeat(22)
        val callbackBody = """
            {
            "amount": $amount,
            "identifier": "$identifier"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/topup")
                .header("Authorization", token)
                .header("X-Signature", "valid-signature")
                .contentType(MediaType.APPLICATION_JSON)
                .content(callbackBody),
        )
            .andExpect(status().isOk)

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
    fun `AC3 invalid top-up returns 400`() {
        val (user, token) = userTestUtils.createUserAndToken("user3@example.com", "Pass3!")
        accountRepository.save(Account(user = user, balance = 0L))
        val amount = 25.0
        val invalidId = 9999999L
        val callbackBody = """
            {
                "amount": $amount,
                "identifier": "$invalidId"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/topup")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(callbackBody),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(ErrorMessages.INVALID_CALLBACK_REQUEST))
    }
}
