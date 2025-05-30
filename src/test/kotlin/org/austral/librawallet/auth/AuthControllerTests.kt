package org.austral.librawallet.auth

import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.account.repository.TransactionRepository
import org.austral.librawallet.auth.repository.UserRepository
import org.austral.librawallet.shared.constants.ErrorMessages
import org.austral.librawallet.util.DatabaseInitializationService
import org.austral.librawallet.util.IntegrationTestBase
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthControllerTests : IntegrationTestBase() {

    @Autowired
    private lateinit var databaseInitializationService: DatabaseInitializationService

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var userRepository: UserRepository

    @Autowired
    lateinit var passwordEncoder: PasswordEncoder

    private val registerUrl = "/api/auth/register"
    private val loginUrl = "/api/auth/login"

    @BeforeEach
    fun setup() {
        databaseInitializationService.clean()
    }

    @Test
    fun `AC1 successful registration returns 201 and user id`() {
        val requestBody = """{"email":"test@example.com","password":"Passw0rd!"}"""
        mockMvc.perform(
            post(registerUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").isNumber)
            .andExpect(jsonPath("$.email").value("test@example.com"))
            .andExpect(jsonPath("$.password").doesNotExist())
            .andReturn()

        // verify password hashing
        val user = userRepository.findByEmail("test@example.com").orElseThrow()
        assertNotEquals("Passw0rd!", user.password)
        assertTrue(passwordEncoder.matches("Passw0rd!", user.password))
    }

    @Test
    fun `AC2 duplicate registration returns 409`() {
        val body = """{"email":"test2@example.com","password":"Passw0rd!"}"""
        // first registration
        mockMvc.perform(
            post(registerUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body),
        ).andExpect(status().isCreated)

        // duplicate registration
        mockMvc.perform(
            post(registerUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body),
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.error").value(ErrorMessages.EMAIL_ALREADY_REGISTERED))
    }

    @Test
    fun `AC3-1 invalid email returns 400`() {
        val invalidEmail = """{"email":"invalid-email","password":"Passw0rd!"}"""
        mockMvc.perform(
            post(registerUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidEmail),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `AC3-2 weak password returns 400`() {
        val weakPassword = """{"email":"user3@example.com","password":"weak"}"""
        mockMvc.perform(
            post(registerUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(weakPassword),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `AC5 successful login returns 200 and JWT`() {
        val password = "Passw0rd!"
        val body = """{"email":"login@example.com","password":"$password"}"""

        // First, register
        mockMvc.perform(
            post(registerUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body),
        ).andExpect(status().isCreated)

        // Then, log in, which should be successful
        mockMvc.perform(
            post(loginUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.token").isString)
    }

    @Test
    fun `AC6 unsuccessful login returns 401`() {
        val body = """{"email":"noone@example.com","password":"DoesntMatter1!"}"""
        mockMvc.perform(
            post(loginUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body),
        )
            .andExpect(status().isUnauthorized)
    }
}
