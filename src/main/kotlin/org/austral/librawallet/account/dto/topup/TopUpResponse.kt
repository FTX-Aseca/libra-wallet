package org.austral.librawallet.account.dto.topup

/**
 * Response payload after creating a top-up order.
 */
data class TopUpResponse(
    val id: Long,
    val status: String,
)
