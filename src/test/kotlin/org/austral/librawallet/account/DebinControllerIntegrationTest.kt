package org.austral.librawallet.account

import org.austral.librawallet.account.entity.DebinRequest
import org.austral.librawallet.account.entity.DebinStatus
import org.austral.librawallet.account.repository.DebinRequestRepository
import org.austral.librawallet.shared.constants.ErrorMessages
import org.austral.librawallet.shared.formatters.formattedDoubleToCents
import org.austral.librawallet.util.DatabaseInitializationService
import org.austral.librawallet.util.IntegrationTestBase
import org.austral.librawallet.util.UserTestUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DebinControllerIntegrationTest : IntegrationTestBase() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var databaseInitializationService: DatabaseInitializationService

    @Autowired
    private lateinit var debinRequestRepository: DebinRequestRepository

    @Autowired
    private lateinit var userTestUtils: UserTestUtils

    @BeforeEach
    fun setup() {
        databaseInitializationService.clean()
    }

    @Test
    fun `AC2 DEBIN request returns 201 with order details`() {
        val (_, _, token) = userTestUtils.createSetupData("user1@example.com", "Pass1!")
        val amount = 75.50
        val requestBody = """{ "amount": $amount }"""

        mockMvc.perform(
            post("/api/debin/request")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.status").value(DebinStatus.PENDING.name))
    }

    @Test
    fun `AC3 DEBIN callback valid transitions to COMPLETED and credits user`() {
        val (_, account, token) = userTestUtils.createSetupData("user2@example.com", "Pass2!")
        val amount = 50.0
        // create DEBIN request via service or repository
        val debin = debinRequestRepository.save(
            DebinRequest(
                account = account,
                amount = formattedDoubleToCents(amount),
                status = DebinStatus.PENDING,
            ),
        )

        val callbackBody = """
            { "id": ${debin.id} }
        """.trimIndent()

        mockMvc.perform(
            post("/api/debin/callback")
                .header("Authorization", token)
                .header("X-Signature", "valid-signature")
                .contentType(MediaType.APPLICATION_JSON)
                .content(callbackBody),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(DebinStatus.COMPLETED.name))

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
    fun `AC5 invalid callback returns 400`() {
        val (_, account, token) = userTestUtils.createSetupData("user3@example.com", "Pass3!")
        val amount = 25.0
        debinRequestRepository.save(
            DebinRequest(
                account = account,
                amount = formattedDoubleToCents(amount),
                status = DebinStatus.PENDING,
            ),
        )
        val invalidId = 99999L
        val callbackBody = """
            { "id": $invalidId }
        """.trimIndent()

        mockMvc.perform(
            post("/api/debin/callback")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(callbackBody),
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value(ErrorMessages.INVALID_CALLBACK_REQUEST))
    }
}
