package org.austral.librawallet.account.repository

import org.austral.librawallet.account.entity.Transaction
import org.springframework.data.jpa.repository.JpaRepository

interface TransactionRepository : JpaRepository<Transaction, Long> {
    fun findByAccountId(accountId: Long): List<Transaction>

    /**
     * Find transactions for an account sorted by timestamp in descending order (newest first)
     */
    fun findByAccountIdOrderByTimestampDesc(accountId: Long): List<Transaction>
}
