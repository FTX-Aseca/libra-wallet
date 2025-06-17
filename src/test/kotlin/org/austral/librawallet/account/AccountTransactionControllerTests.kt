package org.austral.librawallet.account

import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.account.entity.Transaction
import org.austral.librawallet.account.entity.TransactionType
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.account.repository.TransactionRepository
import org.austral.librawallet.shared.formatters.formattedDoubleToCents
import org.austral.librawallet.util.DatabaseInitializationService
import org.austral.librawallet.util.IntegrationTestBase
import org.austral.librawallet.util.UserTestUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertEquals

@SpringBootTest(properties = ["spring.main.allow-bean-definition-overriding=true"])
@Import(DebinControllerIntegrationTest.TestConfig::class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AccountTransactionControllerTests : IntegrationTestBase() {

    @Autowired
    private lateinit var databaseInitializationService: DatabaseInitializationService

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var transactionRepository: TransactionRepository

    @Autowired
    lateinit var userTestUtils: UserTestUtils

    private val transactionsUrl = "/api/accounts/{accountId}/transactions"

    @BeforeEach
    fun setup() {
        databaseInitializationService.clean()
    }

    private fun createTransactions(account: Account, type: TransactionType, amount: Double) {
        val transaction = Transaction(
            account = account,
            otherAccount = account,
            type = type,
            amount = formattedDoubleToCents(amount),
        )
        transactionRepository.save(transaction)
    }

    private fun buildTransactionRequestJson(type: TransactionType, amount: Double, description: String) = """
        {
            "type": "${type.name}",
            "amount": $amount,
            "description": "$description"
        }
    """.trimIndent()

    @Test
    fun `AC1-1 POST with valid payload returns HTTP 201 and persisted transaction JSON for income`() {
        val initialBalance = 100.00
        val depositAmount = 50.00
        val description = "Deposit"
        val (user, jwt) = userTestUtils.createUserAndToken("user5@example.com", "Passw0rd!")
        val account = accountRepository.save(Account(user = user, balance = formattedDoubleToCents(initialBalance)))

        val requestBody = buildTransactionRequestJson(TransactionType.INCOME, depositAmount, description)

        mockMvc.perform(
            post(transactionsUrl, account.id!!)
                .header("Authorization", jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.type").value("INCOME"))
            .andExpect(jsonPath("$.amount").value(depositAmount))
            .andExpect(jsonPath("$.timestamp").exists())

        // Verify account balance was updated
        val updatedAccount = accountRepository.findById(account.id!!).get()
        assertEquals(
            formattedDoubleToCents(initialBalance + depositAmount),
            updatedAccount.balance,
            "Account balance should be updated after deposit",
        )
    }

    @Test
    fun `AC1-2 POST with valid payload returns HTTP 201 and persisted transaction JSON for expense`() {
        val initialBalance = 100.00
        val withdrawalAmount = 30.00
        val description = "Withdrawal"
        val (user, jwt) = userTestUtils.createUserAndToken("user6@example.com", "Passw0rd!")
        val account = accountRepository.save(Account(user = user, balance = formattedDoubleToCents(initialBalance)))

        val requestBody = buildTransactionRequestJson(TransactionType.EXPENSE, withdrawalAmount, description)

        mockMvc.perform(
            post(transactionsUrl, account.id!!)
                .header("Authorization", jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.type").value("EXPENSE"))
            .andExpect(jsonPath("$.amount").value(withdrawalAmount))
            .andExpect(jsonPath("$.timestamp").exists())

        // Verify account balance was updated
        val updatedAccount = accountRepository.findById(account.id!!).get()
        assertEquals(
            formattedDoubleToCents(initialBalance - withdrawalAmount),
            updatedAccount.balance,
            "Account balance should be reduced after withdrawal",
        )
    }

    @Test
    fun `AC2-1 account with no transactions returns empty array`() {
        val (user, jwt) = userTestUtils.createUserAndToken("user4@example.com", "Passw0rd!")
        val account = accountRepository.save(Account(user = user, balance = formattedDoubleToCents(10.00)))

        mockMvc.perform(
            get(transactionsUrl, account.id!!)
                .header("Authorization", jwt)
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty())
    }

    @Test
    fun `AC2-2 GET returns a JSON array of transactions sorted descending by timestamp`() {
        val (user, jwt) = userTestUtils.createUserAndToken("user1@example.com", "Passw0rd!")
        val accountBalance = 100.00
        val expenseAmount = 25.00 // in $ since it is received in the json response
        val incomeAmount = 50.00 // same as above
        val account = accountRepository.save(Account(user = user, balance = formattedDoubleToCents(accountBalance)))

        createTransactions(account, TransactionType.EXPENSE, expenseAmount)
        createTransactions(account, TransactionType.INCOME, incomeAmount)

        mockMvc.perform(
            get(transactionsUrl, account.id!!)
                .header("Authorization", jwt)
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].type").value("INCOME")) // Most recent should be first
            .andExpect(jsonPath("$[0].amount").value(incomeAmount))
            .andExpect(jsonPath("$[0].timestamp").exists())
            .andExpect(jsonPath("$[1].type").value("EXPENSE"))
            .andExpect(jsonPath("$[1].amount").value(expenseAmount))
            .andExpect(jsonPath("$[1].timestamp").exists())
    }

    @Test
    fun `AC3-1 unauthenticated requests return HTTP 401`() {
        val (user, _) = userTestUtils.createUserAndToken("user2@example.com", "Passw0rd!")
        val account = accountRepository.save(Account(user = user, balance = formattedDoubleToCents(50.00)))

        mockMvc.perform(
            get(transactionsUrl, account.id!!),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `AC3-2 requests by non-owners return HTTP 403`() {
        val (owner, _) = userTestUtils.createUserAndToken("owner@example.com", "OwnerPass1!")
        val (intruder, intruderJwt) = userTestUtils.createUserAndToken("intruder@example.com", "IntruderPass2!")

        val ownerAccount = accountRepository.save(Account(user = owner, balance = formattedDoubleToCents(20.00)))
        accountRepository.save(Account(user = intruder, balance = formattedDoubleToCents(20.00)))

        mockMvc.perform(
            get(transactionsUrl, ownerAccount.id!!)
                .header("Authorization", intruderJwt),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `AC4 non-existent accountId returns HTTP 404`() {
        val (_, jwt) = userTestUtils.createUserAndToken("user3@example.com", "Passw0rd!")
        val nonexistentId = 9999999L

        mockMvc.perform(
            get(transactionsUrl, nonexistentId)
                .header("Authorization", jwt),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").exists())
    }

    // AC5 is for response time (skipped)

    @Test
    fun `AC6 expense transaction with insufficient funds returns 409`() {
        val initialBalance = 20.00
        val withdrawalAmount = 50.00 // More than balance
        val description = "Too Large Withdrawal"
        val (user, jwt) = userTestUtils.createUserAndToken("user7@example.com", "Passw0rd!")
        val account = accountRepository.save(Account(user = user, balance = formattedDoubleToCents(initialBalance)))

        val requestBody = buildTransactionRequestJson(TransactionType.EXPENSE, withdrawalAmount, description)

        mockMvc.perform(
            post(transactionsUrl, account.id!!)
                .header("Authorization", jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").exists())

        val updatedAccount = accountRepository.findById(account.id!!).get()
        assertEquals(
            formattedDoubleToCents(initialBalance),
            updatedAccount.balance,
            "Account balance should not change after failed transaction",
        )
    }
}
