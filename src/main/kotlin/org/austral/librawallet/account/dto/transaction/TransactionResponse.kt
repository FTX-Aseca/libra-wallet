package org.austral.librawallet.account.dto.transaction

import org.austral.librawallet.account.entity.TransactionType
import java.time.LocalDateTime

/**
 * Response for a transaction history entry with type and formatted amount.
 */
data class TransactionResponse(
    val type: TransactionType,
    val amount: Double,
    val timestamp: LocalDateTime?,
    val description: String? = null,
)
