package org.austral.librawallet.account.dto

import org.austral.librawallet.account.entity.TransactionType

/**
 * Request to create a direct transaction on an account.
 */
data class TransactionRequest(
    val type: TransactionType,
    val amount: Double,
    val description: String? = null,
)
