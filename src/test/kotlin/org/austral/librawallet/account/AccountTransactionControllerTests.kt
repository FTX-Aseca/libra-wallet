package org.austral.librawallet.account

import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.account.entity.Transaction
import org.austral.librawallet.account.entity.TransactionType
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.account.repository.TransactionRepository
import org.austral.librawallet.auth.entity.User
import org.austral.librawallet.auth.repository.UserRepository
import org.austral.librawallet.auth.util.JwtUtil
import org.austral.librawallet.util.DatabaseInitializationService
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
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountTransactionControllerTests {

    @Autowired
    private lateinit var databaseInitializationService: DatabaseInitializationService

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var transactionRepository: TransactionRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var jwtUtils: JwtUtil

    private val transactionsUrl = "/api/accounts/{accountId}/transactions"

    @BeforeEach
    fun setup() {
        databaseInitializationService.clean()
    }

    private fun createUserAndToken(email: String, password: String): Pair<User, String> {
        val hashed = passwordEncoder.encode(password)
        val user = userRepository.save(User(email = email, password = hashed))
        val token = "Bearer ${jwtUtils.generateToken(user)}"
        return user to token
    }

    private fun createTransactions(account: Account, otherAccount: Account) {
        val income = Transaction(
            account = account,
            otherAccount = otherAccount,
            type = TransactionType.INCOME,
            amount = 5000L, // $50.00
            timestamp = LocalDateTime.now().minusDays(1),
        )

        val expense = Transaction(
            account = account,
            otherAccount = otherAccount,
            type = TransactionType.EXPENSE,
            amount = 2500L, // $25.00
            timestamp = LocalDateTime.now(),
        )

        transactionRepository.save(income)
        transactionRepository.save(expense)
    }

    @Test
    fun `AC1-1 POST with valid payload returns HTTP 201 and persisted transaction JSON for income`() {
        val initialBalance = 100.00
        val depositAmount = 50.00
        val (user, jwt) = createUserAndToken("user5@example.com", "Passw0rd!")
        val account = accountRepository.save(Account(user = user, balance = (initialBalance * 100).toLong()))

        val requestBody = """
            {
                "type": "INCOME",
                "amount": $depositAmount,
                "description": "Deposit"
            }
        """.trimIndent()

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
        assert(updatedAccount.balance == ((initialBalance + depositAmount) * 100).toLong())
    }

    @Test
    fun `AC1-2 POST with valid payload returns HTTP 201 and persisted transaction JSON for expense`() {
        val initialBalance = 100.00
        val withdrawalAmount = 30.00
        val (user, jwt) = createUserAndToken("user6@example.com", "Passw0rd!")
        val account = accountRepository.save(Account(user = user, balance = (initialBalance * 100).toLong()))

        val requestBody = """
            {
                "type": "EXPENSE",
                "amount": $withdrawalAmount,
                "description": "Withdrawal"
            }
        """.trimIndent()

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
        assert(updatedAccount.balance == ((initialBalance - withdrawalAmount) * 100).toLong())
    }

    @Test
    fun `AC2-1 account with no transactions returns empty array`() {
        val (user, jwt) = createUserAndToken("user4@example.com", "Passw0rd!")
        val account = accountRepository.save(Account(user = user, balance = 1000L))

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
        val (user, jwt) = createUserAndToken("user1@example.com", "Passw0rd!")
        val (otherUser, _) = createUserAndToken("other@example.com", "Passw0rd!")

        val account = accountRepository.save(Account(user = user, balance = 10000L))
        val otherAccount = accountRepository.save(Account(user = otherUser, balance = 5000L))

        createTransactions(account, otherAccount)

        mockMvc.perform(
            get(transactionsUrl, account.id!!)
                .header("Authorization", jwt)
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].type").value("EXPENSE")) // Most recent should be first
            .andExpect(jsonPath("$[0].amount").value(25.00))
            .andExpect(jsonPath("$[0].timestamp").exists())
            .andExpect(jsonPath("$[1].type").value("INCOME"))
            .andExpect(jsonPath("$[1].amount").value(50.00))
            .andExpect(jsonPath("$[1].timestamp").exists())
    }

    @Test
    fun `AC3-1 unauthenticated requests return HTTP 401`() {
        val (user, _) = createUserAndToken("user2@example.com", "Passw0rd!")
        val account = accountRepository.save(Account(user = user, balance = 5000L))

        mockMvc.perform(
            get(transactionsUrl, account.id!!),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `AC3-2 requests by non-owners return HTTP 403`() {
        val (owner, _) = createUserAndToken("owner@example.com", "OwnerPass1!")
        val (intruder, intruderJwt) = createUserAndToken("intruder@example.com", "IntruderPass2!")

        val ownerAccount = accountRepository.save(Account(user = owner, balance = 2000L))
        val intruderAccount = accountRepository.save(Account(user = intruder, balance = 2000L))

        mockMvc.perform(
            get(transactionsUrl, ownerAccount.id!!)
                .header("Authorization", intruderJwt),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `AC4 non-existent accountId returns HTTP 404`() {
        val (user, jwt) = createUserAndToken("user3@example.com", "Passw0rd!")
        val nonexistentId = 9999999L

        mockMvc.perform(
            get(transactionsUrl, nonexistentId)
                .header("Authorization", jwt),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `AC6 expense transaction with insufficient funds returns 409`() { // AC5 is for response time (skipped)
        val initialBalance = 20.00
        val withdrawalAmount = 50.00 // More than balance
        val (user, jwt) = createUserAndToken("user7@example.com", "Passw0rd!")
        val account = accountRepository.save(Account(user = user, balance = (initialBalance * 100).toLong()))

        val requestBody = """
            {
                "type": "EXPENSE",
                "amount": $withdrawalAmount,
                "description": "Too Large Withdrawal"
            }
        """.trimIndent()

        mockMvc.perform(
            post(transactionsUrl, account.id!!)
                .header("Authorization", jwt)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").exists())

        val updatedAccount = accountRepository.findById(account.id!!).get()
        assert(updatedAccount.balance == (initialBalance * 100).toLong())
    }
}
