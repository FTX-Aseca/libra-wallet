package org.austral.librawallet.account

import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.auth.entity.User
import org.austral.librawallet.auth.repository.UserRepository
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountControllerTests {

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
    lateinit var userTestUtils: UserTestUtils

    private val balanceUrl = "/api/accounts/{accountId}/balance"

    @BeforeEach
    fun setup() {
        databaseInitializationService.clean()
    }

    @Test
    fun `AC1 authenticated user gets 200 and balance JSON`() {
        val (user, jwt) = userTestUtils.createUserAndToken("user1@example.com", "Passw0rd!")
        val balance = 1234.56
        val balanceInCents: Long = formattedDoubleToCents(balance)
        val account = accountRepository.save(Account(user = user, balance = balanceInCents))

        mockMvc.perform(
            get(balanceUrl, account.id!!)
                .header("Authorization", jwt)
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(balance))
            .andReturn()
    }

    @Test
    fun `AC2 balance formatted as non-negative double with two decimals`() {
        val (user, jwt) = userTestUtils.createUserAndToken("user2@example.com", "Passw0rd2!")
        val balance = 10.50
        val account = accountRepository.save(Account(user = user, balance = formattedDoubleToCents(balance)))

        mockMvc.perform(
            get(balanceUrl, account.id!!)
                .header("Authorization", jwt),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(balance))
    }

    @Test
    fun `AC3 unauthenticated request yields 401`() {
        val user =
            userRepository.save(User(email = "user3@example.com", password = passwordEncoder.encode("Passw0rd3!")))

        val account = accountRepository.save(Account(user = user, balance = 999L))

        mockMvc.perform(
            get(balanceUrl, account.id!!),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `AC4 user who does not own the account gets 403`() {
        val (owner, _) = userTestUtils.createUserAndToken("owner@example.com", "OwnerPass1!")
        val (_, intruderJwt) = userTestUtils.createUserAndToken("intruder@example.com", "IntruderPass2!")
        val account = accountRepository.save(Account(user = owner, balance = 2000L))

        mockMvc.perform(
            get(balanceUrl, account.id!!)
                .header("Authorization", intruderJwt),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `AC5 non-existent accountId gives 404`() {
        val (_, jwt) = userTestUtils.createUserAndToken("user5@example.com", "Passw0rd5!")
        val nonexistentId = 9999999L

        mockMvc.perform(
            get(balanceUrl, nonexistentId)
                .header("Authorization", jwt),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").exists())
    }
}
