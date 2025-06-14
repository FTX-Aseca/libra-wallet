package org.austral.librawallet.account

import org.austral.librawallet.account.dto.IdentifierType
import org.austral.librawallet.account.repository.DebinRequestRepository
import org.austral.librawallet.account.service.DebinIntegrationService
import org.austral.librawallet.account.service.FakeDebinIntegrationService
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest(properties = [
    "spring.main.allow-bean-definition-overriding=true",
    "external.api.base-url=http://external_api:5001"
])
@Import(DebinControllerIntegrationTest.TestConfig::class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
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
    fun `AC2 DEBIN request returns 200 with order details`() {
        val (_, _, token) = userTestUtils.createSetupData("user1@example.com", "Pass1!")
        val amount = 75.50
        val identifier = "0".repeat(22)
        val requestBody = """
            {
                "amount": $amount,
                "identifierType": "${IdentifierType.CVU}",
                "fromIdentifier": "$identifier"
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/debin/request")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.identifier").value(identifier))
            .andExpect(jsonPath("$.amount").isNumber)
    }

    @TestConfiguration
    class TestConfig {
        @Bean("debinIntegrationServiceImpl")
        @Primary
        fun debinIntegrationService(): DebinIntegrationService = FakeDebinIntegrationService()
    }
}
