package org.austral.librawallet.account.entity

import jakarta.persistence.*

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
