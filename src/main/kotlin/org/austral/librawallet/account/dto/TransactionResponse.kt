package org.austral.librawallet.account.dto

import org.austral.librawallet.account.entity.TransactionType

/**
 * Response for a transaction history entry with type and formatted amount.
 */
data class TransactionResponse(
    val type: TransactionType,
    val amount: Double,
)
