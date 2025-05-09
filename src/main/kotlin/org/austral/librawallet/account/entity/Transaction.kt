package org.austral.librawallet.account.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.PrePersist
import java.time.LocalDateTime

@Entity
class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    val account: Account,

    @ManyToOne
    @JoinColumn(name = "other_account_id", nullable = false)
    val otherAccount: Account,

    @Enumerated(EnumType.STRING)
    val type: TransactionType,

    // amount in cents
    val amount: Long,

    var timestamp: LocalDateTime? = null,

    // optional description of the transaction
    val description: String? = null,
) {
    @PrePersist
    fun onCreate() {
        timestamp = LocalDateTime.now()
    }
}
