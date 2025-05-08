package org.austral.librawallet.account.repository

import org.austral.librawallet.account.entity.Account
import org.springframework.data.jpa.repository.JpaRepository

interface AccountRepository : JpaRepository<Account, Long> {
    fun findByUserId(userId: Long): Account?

    fun findByAlias(alias: String): Account?

    fun findByCvu(cvu: String): Account?
}
