package org.austral.librawallet.account.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

/**
 * Entity representing a DEBIN request.
 */
@Entity
class DebinRequest(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    var account: Account,

    /** amount in cents */
    var amount: Long,

    @Enumerated(EnumType.STRING)
    var status: DebinStatus,
)
