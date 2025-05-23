package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.IdentifierType
import org.austral.librawallet.account.entity.Account
import org.austral.librawallet.account.exceptions.ForbiddenException
import org.austral.librawallet.account.exceptions.NotFoundException
import org.austral.librawallet.account.repository.AccountRepository
import org.austral.librawallet.auth.entity.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Optional

class AccountServiceTest {

    private val accountRepository: AccountRepository = mock()
    private val accountService = AccountService(accountRepository)

    @Test
    fun `getBalance should return balance when account exists and belongs to user`() {
        // Given
        val accountId = 1L
        val userId = "1"
        val user = User(id = 1L, email = "test@example.com", password = "password")
        val account = Account(id = accountId, user = user, balance = 1000L)
        whenever(accountRepository.findById(accountId)).thenReturn(Optional.of(account))

        // When
        val result = accountService.getBalance(accountId, userId)

        // Then
        assert(result == 10.0) // 1000 cents = 10.0 dollars
    }

    @Test
    fun `getBalance should throw NotFoundException when account does not exist`() {
        // Given
        val accountId = 1L
        val userId = "1"
        whenever(accountRepository.findById(accountId)).thenReturn(Optional.empty())

        // When/Then
        assertThrows<NotFoundException> {
            accountService.getBalance(accountId, userId)
        }
    }

    @Test
    fun `getBalance should throw ForbiddenException when account belongs to different user`() {
        // Given
        val accountId = 1L
        val userId = "2"
        val user = User(id = 1L, email = "test@example.com", password = "password")
        val account = Account(id = accountId, user = user, balance = 1000L)
        whenever(accountRepository.findById(accountId)).thenReturn(Optional.of(account))

        // When/Then
        assertThrows<ForbiddenException> {
            accountService.getBalance(accountId, userId)
        }
    }

    @Test
    fun `getBalance should throw ForbiddenException when userId is invalid`() {
        // Given
        val accountId = 1L
        val userId = "not-a-number"
        val user = User(id = 1L, email = "test@example.com", password = "password")
        val account = Account(id = accountId, user = user, balance = 1000L)
        whenever(accountRepository.findById(accountId)).thenReturn(Optional.of(account))

        // When/Then
        assertThrows<ForbiddenException> {
            accountService.getBalance(accountId, userId)
        }
    }

    @Test
    fun `getAccountOrThrow should return account when found by alias`() {
        // Given
        val alias = "test.alias"
        val account = Account(alias = alias, user = User(email = "test@example.com", password = "password"))
        whenever(accountRepository.findByAlias(alias)).thenReturn(account)

        // When
        val result = accountService.getAccountOrThrow(IdentifierType.ALIAS, alias)

        // Then
        assert(result == account)
    }

    @Test
    fun `getAccountOrThrow should return account when found by CVU`() {
        // Given
        val cvu = "1234567890123456789012"
        val account = Account(cvu = cvu, user = User(email = "test@example.com", password = "password"))
        whenever(accountRepository.findByCvu(cvu)).thenReturn(account)

        // When
        val result = accountService.getAccountOrThrow(IdentifierType.CVU, cvu)

        // Then
        assert(result == account)
    }

    @Test
    fun `getAccountOrThrow should throw NotFoundException when account not found by alias`() {
        // Given
        val alias = "test.alias"
        whenever(accountRepository.findByAlias(alias)).thenReturn(null)

        // When/Then
        assertThrows<NotFoundException> {
            accountService.getAccountOrThrow(IdentifierType.ALIAS, alias)
        }
    }

    @Test
    fun `getAccountOrThrow should throw NotFoundException when account not found by CVU`() {
        // Given
        val cvu = "1234567890123456789012"
        whenever(accountRepository.findByCvu(cvu)).thenReturn(null)

        // When/Then
        assertThrows<NotFoundException> {
            accountService.getAccountOrThrow(IdentifierType.CVU, cvu)
        }
    }
}
