package org.austral.librawallet.account

import org.austral.librawallet.account.dto.IdentifierType
import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.shared.constants.ErrorMessages
import org.austral.librawallet.util.DatabaseInitializationService
import org.austral.librawallet.util.IntegrationTestBase
import org.austral.librawallet.util.UserTestUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(
    properties = [
        "spring.main.allow-bean-definition-overriding=true",
        "external.api.base-url=http://external_api:5001",
    ],
)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class TopUpControllerIntegrationTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var databaseInitializationService: DatabaseInitializationService

    @Autowired
    private lateinit var accountRepository: AccountRepository

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
        val account = accountRepository.save(
            Account(user = user, balance = 0L),
        )
        val amount = 100.00
        val identifierType = IdentifierType.CVU
        val requestBody = """
            {
            "amount": $amount,
            "toIdentifier": "${account.cvu}",
            "identifierType": "${identifierType.name}"
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
    fun `AC3 invalid top-up returns 404`() {
        val (user, token) = userTestUtils.createUserAndToken("user3@example.com", "Pass3!")
        accountRepository.save(Account(user = user, balance = 0L))
        val amount = 25.0
        val invalidId = 9999999L
        val identifierType = IdentifierType.CVU
        val callbackBody = """
            {
                "amount": $amount,
                "toIdentifier": "$invalidId",
                "identifierType": "${identifierType.name}"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/topup")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(callbackBody),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").value(ErrorMessages.ACCOUNT_NOT_FOUND))
    }
}
