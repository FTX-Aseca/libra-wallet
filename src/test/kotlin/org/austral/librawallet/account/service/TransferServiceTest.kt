package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.IdentifierType
import org.austral.librawallet.account.dto.transfer.TransferRequest
import org.austral.librawallet.account.dto.transfer.TransferResponse
import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.account.repository.TransactionRepository
import org.austral.librawallet.auth.entity.User
import org.austral.librawallet.account.exceptions.ForbiddenException
import org.austral.librawallet.account.exceptions.NotFoundException
import org.austral.librawallet.auth.exception.ConflictException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.junit.jupiter.api.extension.ExtendWith
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class TransferServiceTest {

    @Mock
    private lateinit var accountRepository: AccountRepository

    @Mock
    private lateinit var accountService: AccountService

    @Mock
    private lateinit var transactionRepository: TransactionRepository

    @InjectMocks
    private lateinit var transferService: TransferService

    private lateinit var user: User
    private lateinit var senderAccount: Account
    private lateinit var receiverAccount: Account

    @BeforeEach
    fun setup() {
        user = User(id = 1L, email = "test@example.com", password = "password")
        senderAccount = Account(id = 1L, user = user, balance = 10000L) // 100.00
        receiverAccount = Account(id = 2L, user = User(id = 2L, email = "receiver@example.com", password = "password"), balance = 5000L) // 50.00
    }

    @Test
    fun `transfer should update balances and create transactions when successful`() {
        // Given
        val request = TransferRequest(
            toIdentifier = "receiver-cvu",
            identifierType = IdentifierType.CVU,
            amount = 50.0
        )
        `when`(accountRepository.findByUserId(1L)).thenReturn(senderAccount)
        `when`(accountService.getAccountOrThrow(IdentifierType.CVU, "receiver-cvu")).thenReturn(receiverAccount)
        `when`(accountRepository.save(any(Account::class.java))).thenAnswer { it.arguments[0] }
        `when`(transactionRepository.save(any())).thenAnswer { it.arguments[0] }

        // When
        val response = transferService.transfer(request, "1")

        // Then
        // sender: 100.00 - 50.00 = 50.00, receiver: 50.00 + 50.00 = 100.00
        assert(response.fromBalance == 50.0)
        assert(response.toBalance == 100.0)
        verify(accountRepository, times(2)).save(any(Account::class.java))
        verify(transactionRepository, times(2)).save(any())
    }

    @Test
    fun `transfer should throw ConflictException when insufficient funds`() {
        // Given
        val request = TransferRequest(
            toIdentifier = "receiver-cvu",
            identifierType = IdentifierType.CVU,
            amount = 200.0
        )
        `when`(accountRepository.findByUserId(1L)).thenReturn(senderAccount)
        `when`(accountService.getAccountOrThrow(IdentifierType.CVU, "receiver-cvu")).thenReturn(receiverAccount)

        // When/Then
        assertThrows<ConflictException> {
            transferService.transfer(request, "1")
        }
    }

    @Test
    fun `transfer should throw ForbiddenException when user is not account owner`() {
        // Given
        val request = TransferRequest(
            toIdentifier = "receiver-cvu",
            identifierType = IdentifierType.CVU,
            amount = 50.0
        )

        // When/Then
        assertThrows<ForbiddenException> {
            transferService.transfer(request, "not-a-number")
        }
    }

    @Test
    fun `transfer should throw NotFoundException when sender account not found`() {
        // Given
        val request = TransferRequest(
            toIdentifier = "receiver-cvu",
            identifierType = IdentifierType.CVU,
            amount = 50.0
        )
        `when`(accountRepository.findByUserId(1L)).thenReturn(null)

        // When/Then
        assertThrows<NotFoundException> {
            transferService.transfer(request, "1")
        }
    }

    @Test
    fun `getTransactions should return list of transactions`() {
        // Given
        val tx = mock(org.austral.librawallet.account.entity.Transaction::class.java)
        `when`(tx.type).thenReturn(org.austral.librawallet.account.entity.TransactionType.EXPENSE)
        `when`(tx.amount).thenReturn(5000L)
        `when`(tx.timestamp).thenReturn(null)
        `when`(tx.description).thenReturn("Test transfer")
        `when`(accountRepository.findByUserId(1L)).thenReturn(senderAccount)
        `when`(transactionRepository.findByAccountId(1L)).thenReturn(listOf(tx))

        // When
        val result = transferService.getTransactions("1")

        // Then
        assert(result.size == 1)
        assert(result[0].amount == 50.0)
        assert(result[0].type == org.austral.librawallet.account.entity.TransactionType.EXPENSE)
        assert(result[0].description == "Test transfer")
    }

    @Test
    fun `getTransactions should throw ForbiddenException when userId is invalid`() {
        assertThrows<ForbiddenException> {
            transferService.getTransactions("not-a-number")
        }
    }

    @Test
    fun `getTransactions should throw NotFoundException when account not found`() {
        `when`(accountRepository.findByUserId(999L)).thenReturn(null)
        assertThrows<NotFoundException> {
            transferService.getTransactions("999")
        }
    }
} 