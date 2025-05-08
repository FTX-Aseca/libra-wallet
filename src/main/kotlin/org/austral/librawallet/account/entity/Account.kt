package org.austral.librawallet.account.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.PrePersist
import org.austral.librawallet.auth.entity.User
import org.austral.librawallet.shared.util.AliasGenerator
import org.austral.librawallet.shared.util.CVUGenerator

// TODO check if account and user are 1-1 relationship
@Entity
class Account(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    var user: User,

    var balance: Long = 0L,

    @Column(unique = true, nullable = false)
    var alias: String = "",

    @Column(unique = true, nullable = false, length = 22)
    var cvu: String = "",
) : java.io.Serializable {

    @PrePersist
    fun ensureAliasAndCvu() {
        if (alias.isBlank()) alias = AliasGenerator.generate()
        if (cvu.isBlank()) cvu = CVUGenerator.generate()
    }
}
