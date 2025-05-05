package org.austral.librawallet.account.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import org.austral.librawallet.auth.entity.User

// TODO check if account and user are 1-1 relationship
@Entity
class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    var user: User,

    /**
     * Stores the account balance in the smallest currency unit (cents) to prevent floating-point precision errors.
     * This value is later converted to dollar format using the centsToFormattedDouble utility.
     */
    var balance: Long = 0L,
)