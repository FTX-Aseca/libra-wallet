package org.austral.librawallet.account

import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.auth.repository.UserRepository
import org.austral.librawallet.auth.util.JwtUtil
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
class TransferControllerTests {

    @Autowired
    private lateinit var databaseInitializationService: DatabaseInitializationService

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var jwtUtils: JwtUtil

    @Autowired
    lateinit var userTestUtils: UserTestUtils

    @BeforeEach
    fun setup() {
        databaseInitializationService.clean()
    }

    @Test
    fun `AC1 valid transfer returns 200 with JSON containing new balances`() {
        val transferAmount = 30.25
        val initialSenderBalance = 100.00
        val initialReceiverBalance = 50.00

        val (sender, senderToken) = userTestUtils.createUserAndToken("sender@example.com", "Pass1!")
        val (receiver, _) = userTestUtils.createUserAndToken("receiver@example.com", "Pass2!")

        val senderAccount = accountRepository.save(
            Account(user = sender, balance = (initialSenderBalance * 100).toLong()),
        )
        val receiverAccount = accountRepository.save(
            Account(user = receiver, balance = (initialReceiverBalance * 100).toLong()),
        )

        val requestBody = """
            {
                "toIdentifier": "${receiverAccount.alias}",
                "identifierType": "ALIAS",
                "amount": $transferAmount
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/transfers")
                .header("Authorization", senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.fromBalance").value(initialSenderBalance - transferAmount))
            .andExpect(jsonPath("$.toBalance").value(initialReceiverBalance + transferAmount))
    }

    @Test
    fun `AC2 insufficient funds returns 409 Conflict with error`() {
        val transferAmount = 20.00
        val initialSenderBalance = 10.00

        val (sender, senderToken) = userTestUtils.createUserAndToken("sender2@example.com", "Pass3!")
        val (receiver, _) = userTestUtils.createUserAndToken("receiver2@example.com", "Pass4!")

        val senderAccount = accountRepository.save(
            Account(user = sender, balance = (initialSenderBalance * 100).toLong()),
        )
        val receiverAccount = accountRepository.save(
            Account(user = receiver, balance = 0L),
        )

        val requestBody = """
            {
                "toIdentifier": "${receiverAccount.alias}",
                "identifierType": "ALIAS",
                "amount": $transferAmount
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/transfers")
                .header("Authorization", senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `AC3 transactions appear in both users histories via GET transactions`() {
        val transferAmount = 15.50
        val initialSenderBalance = 100.00
        val initialReceiverBalance = 50.00

        val (sender, senderToken) = userTestUtils.createUserAndToken("sender3@example.com", "Pass5!")
        val (receiver, receiverToken) = userTestUtils.createUserAndToken("receiver3@example.com", "Pass6!")

        val senderAccount = accountRepository.save(
            Account(user = sender, balance = (initialSenderBalance * 100).toLong()),
        )
        val receiverAccount = accountRepository.save(
            Account(user = receiver, balance = (initialReceiverBalance * 100).toLong()),
        )

        val requestBody = """
            {
                "toIdentifier": "${receiverAccount.alias}",
                "identifierType": "ALIAS",
                "amount": $transferAmount
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/transfers")
                .header("Authorization", senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        ).andExpect(status().isOk)

        mockMvc.perform(
            get("/api/transactions")
                .header("Authorization", senderToken)
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].type").value("EXPENSE"))
            .andExpect(jsonPath("$[0].amount").value(transferAmount))

        mockMvc.perform(
            get("/api/transactions")
                .header("Authorization", receiverToken)
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].type").value("INCOME"))
            .andExpect(jsonPath("$[0].amount").value(transferAmount))
    }

    @Test
    fun `AC4-1 unauthenticated request yields 401`() {
        val amount = 10.0
        val requestBody = """
            {
                "toIdentifier": "invalid",
                "identifierType": "ALIAS",
                "amount": $amount
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/transfers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        )
            .andExpect(status().isUnauthorized)
    }

    @Test
    fun `AC4-2 invalid receiver yields 404`() {
        val transferAmount = 10.0
        val (sender, senderToken) = userTestUtils.createUserAndToken("sender4@example.com", "Pass7!")

        val senderAccount = accountRepository.save(
            Account(user = sender, balance = (transferAmount * 100).toLong()),
        )

        val requestBody = """
            {
                "toIdentifier": "nonexistent",
                "identifierType": "ALIAS",
                "amount": $transferAmount
            }
        """.trimIndent()

        mockMvc.perform(
            post("/api/transfers")
                .header("Authorization", senderToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").exists())
    }
}
