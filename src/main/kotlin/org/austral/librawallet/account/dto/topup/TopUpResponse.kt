package org.austral.librawallet.account.dto.topup

/**
 * Response payload after creating a top-up operation, containing the CVU and current balance.
 */
data class TopUpResponse(
    val identifier: String,
    val amount: Double,
)
