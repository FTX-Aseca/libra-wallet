package org.austral.librawallet.account

import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.auth.entity.User
import org.austral.librawallet.auth.repository.UserRepository
import org.austral.librawallet.auth.util.JwtUtil
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
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    lateinit var jwtUtils: JwtUtil

    private val balanceUrl = "/api/accounts/{accountId}/balance"

    @BeforeEach
    fun setup() {
        accountRepository.deleteAll()
        userRepository.deleteAll()
    }

    private fun createUserAndToken(email: String, password: String): Pair<User, String> {
        val hashed = passwordEncoder.encode(password)
        val user = userRepository.save(User(email = email, password = hashed))
        val token = "Bearer ${jwtUtils.generateToken(user)}"
        return user to token
    }

    @Test
    fun `AC1 authenticated user gets 200 and balance JSON`() {
        val (user, jwt) = createUserAndToken("user1@example.com", "Passw0rd!")
        val balance = 1234.56
        val balanceInCents: Long = (balance * 100).toLong()
        val account = accountRepository.save(Account(user = user, balance = balanceInCents))

        val mvcResult = mockMvc.perform(
            get(balanceUrl, account.id!!)
                .header("Authorization", jwt)
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value("1234.56"))
            .andReturn()
    }

    @Test
    fun `AC2 balance formatted as non-negative double with two decimals`() {
        val (user, jwt) = createUserAndToken("user2@example.com", "Passw0rd2!")
        val account = accountRepository.save(Account(user = user, balance = (10.5 * 100).toLong()))

        mockMvc.perform(
            get(balanceUrl, account.id!!)
                .header("Authorization", jwt),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.balance").value(10.50))
    }

    @Test
    fun `AC3 unauthenticated request yields 401`() {
        val user = userRepository.save(User(email = "user3@example.com", password = passwordEncoder.encode("Passw0rd3!")))

        val account = accountRepository.save(Account(user = user, balance = 999L))

        mockMvc.perform(
            get(balanceUrl, account.id!!),
        ).andExpect(status().isUnauthorized)
    }

    @Test
    fun `AC4 user who does not own the account gets 403`() {
        val (owner, ownerJwt) = createUserAndToken("owner@example.com", "OwnerPass1!")
        val (intruder, intruderJwt) = createUserAndToken("intruder@example.com", "IntruderPass2!")
        val account = accountRepository.save(Account(user = owner, balance = 2000L))

        mockMvc.perform(
            get(balanceUrl, account.id!!)
                .header("Authorization", intruderJwt),
        ).andExpect(status().isForbidden)
    }

    @Test
    fun `AC5 non-existent accountId gives 404`() {
        val (user, jwt) = createUserAndToken("user5@example.com", "Passw0rd5!")
        val nonexistentId = 9999999L

        mockMvc.perform(
            get(balanceUrl, nonexistentId)
                .header("Authorization", jwt),
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").exists())
    }
}