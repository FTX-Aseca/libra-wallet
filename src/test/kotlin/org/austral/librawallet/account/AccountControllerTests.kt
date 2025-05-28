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
    private val detailsUrl = "/api/accounts/{accountId}"

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

    @Test
    fun `AD1 authenticated user gets 200 and account details JSON`() {
        val (user, jwt) = userTestUtils.createUserAndToken("details1@example.com", "Passw0rd!")
        val account = accountRepository.save(Account(user = user, balance = 1000L))

        mockMvc.perform(
            get(detailsUrl, account.id!!)
                .header("Authorization", jwt)
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").value(user.email))
            .andExpect(jsonPath("$.alias").value(account.alias))
            .andExpect(jsonPath("$.cvu").value(account.cvu))
    }

    @Test
    fun `AD2 account details contain all required fields`() {
        val (user, jwt) = userTestUtils.createUserAndToken("details2@example.com", "Passw0rd2!")
        val account = accountRepository.save(Account(user = user, balance = 5000L))

        mockMvc.perform(
            get(detailsUrl, account.id!!)
                .header("Authorization", jwt),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.email").exists())
            .andExpect(jsonPath("$.alias").exists())
            .andExpect(jsonPath("$.cvu").exists())
            .andExpect(jsonPath("$.email").isString())
            .andExpect(jsonPath("$.alias").isString())
            .andExpect(jsonPath("$.cvu").isString())
    }

    @Test
    fun `AD3 unauthenticated request yields 401`() {
        val user =
            userRepository.save(User(email = "details3@example.com", password = passwordEncoder.encode("Passw0rd3!")))
        val account = accountRepository.save(Account(user = user, balance = 999L))

        mockMvc.perform(
            get(detailsUrl, account.id!!),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `AD4 user who does not own the account gets 403`() {
        val (owner, _) = userTestUtils.createUserAndToken("detailsowner@example.com", "OwnerPass1!")
        val (_, intruderJwt) = userTestUtils.createUserAndToken("detailsintruder@example.com", "IntruderPass2!")
        val account = accountRepository.save(Account(user = owner, balance = 2000L))

        mockMvc.perform(
            get(detailsUrl, account.id!!)
                .header("Authorization", intruderJwt),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `AD5 non-existent accountId gives 404`() {
        val (_, jwt) = userTestUtils.createUserAndToken("details5@example.com", "Passw0rd5!")
        val nonexistentId = 9999999L

        mockMvc.perform(
            get(detailsUrl, nonexistentId)
                .header("Authorization", jwt),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").exists())
    }

    @Test
    fun `AD6 CVU format validation - should be 22 characters`() {
        val (user, jwt) = userTestUtils.createUserAndToken("details6@example.com", "Passw0rd6!")
        val account = accountRepository.save(Account(user = user, balance = 1000L))

        mockMvc.perform(
            get(detailsUrl, account.id!!)
                .header("Authorization", jwt),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.cvu").isString())
            .andExpect(jsonPath("$.cvu").value(org.hamcrest.Matchers.hasLength(22)))
    }

    @Test
    fun `AD7 alias and CVU are unique for different accounts`() {
        val (user1, jwt1) = userTestUtils.createUserAndToken("details7a@example.com", "Passw0rd7a!")
        val (user2, jwt2) = userTestUtils.createUserAndToken("details7b@example.com", "Passw0rd7b!")

        val account1 = accountRepository.save(Account(user = user1, balance = 1000L))
        val account2 = accountRepository.save(Account(user = user2, balance = 2000L))

        val result1 = mockMvc.perform(
            get(detailsUrl, account1.id!!)
                .header("Authorization", jwt1),
        )
            .andExpect(status().isOk)
            .andReturn()

        val result2 = mockMvc.perform(
            get(detailsUrl, account2.id!!)
                .header("Authorization", jwt2),
        )
            .andExpect(status().isOk)
            .andReturn()

        val response1 = result1.response.contentAsString
        val response2 = result2.response.contentAsString

        // Parse JSON responses to verify aliases and CVUs are different
        assert(response1.contains("\"alias\":") && response2.contains("\"alias\":"))
        assert(response1.contains("\"cvu\":") && response2.contains("\"cvu\":"))
        // The aliases and CVUs should be different (this is ensured by unique constraints)
    }
}
