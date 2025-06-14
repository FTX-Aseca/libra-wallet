package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.IdentifierType
import org.austral.librawallet.account.dto.topup.TopUpRequest
import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.account.entity.TopUpOrder
import org.austral.librawallet.account.exceptions.ForbiddenException
import org.austral.librawallet.account.exceptions.NotFoundException
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.account.repository.TopUpOrderRepository
import org.austral.librawallet.account.repository.TransactionRepository
import org.austral.librawallet.auth.entity.User
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class TopUpServiceTest {

    @Mock
    private lateinit var accountRepository: AccountRepository

    @Mock
    private lateinit var topUpOrderRepository: TopUpOrderRepository

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    private lateinit var topUpIntegrationService: TopUpIntegrationService

    private lateinit var topUpService: TopUpService

    @BeforeEach
    fun setup() {
        topUpIntegrationService = FakeTopUpIntegrationService()
        topUpService =
            TopUpService(
                accountRepository,
                topUpOrderRepository,
                transactionRepository = transactionRepository,
                topUpIntegrationService = topUpIntegrationService,
            )
    }

    @Test
    fun `successful top up case`() {
        val jwtId = "1"
        val amount = 20.0
        val cents = (amount * 100).toLong()
        val user = User(email = "a@b.com", password = "pwd")
        val account = Account(id = 1L, user = user, balance = 0L)
        `when`(accountRepository.findByUserId(1L)).thenReturn(account)
        val order = TopUpOrder(id = 5L, account = account, amount = cents)
        `when`(topUpOrderRepository.save(any(TopUpOrder::class.java))).thenReturn(order)

        val response = topUpService.topUp(
            TopUpRequest(
                amount = amount,
                identifierType = IdentifierType.CVU,
                fromIdentifier = "0".repeat(22),
            ),
            jwtId,
        )

        assertEquals(20L, response.amount.toLong())
        verify(topUpOrderRepository).save(any())
    }

    @Test
    fun `invalid jwt user id throws ForbiddenException`() {
        val amount = 10.0
        assertThrows(ForbiddenException::class.java) {
            topUpService.topUp(TopUpRequest(amount, identifierType = IdentifierType.CVU, "0".repeat(22)), "not-a-number")
        }
    }

    @Test
    fun `no account found throws NotFoundException`() {
        val jwtId = "2"
        val amount = 10.0
        `when`(accountRepository.findByUserId(2L)).thenReturn(null)
        assertThrows(NotFoundException::class.java) {
            topUpService.topUp(TopUpRequest(amount, identifierType = IdentifierType.CVU, "0".repeat(22)), jwtId)
        }
    }
}
