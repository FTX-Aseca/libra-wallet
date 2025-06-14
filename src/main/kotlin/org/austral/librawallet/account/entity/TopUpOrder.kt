package org.austral.librawallet.account.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

/**
 * Entity representing a top-up order.
 */
@Entity
class TopUpOrder(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    var account: Account,

    /** amount in cents */
    var amount: Long,
)
