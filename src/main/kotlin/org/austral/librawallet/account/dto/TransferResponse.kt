package org.austral.librawallet.account.dto

/**
 * Response for a peer-to-peer transfer returning updated balances.
 */
data class TransferResponse(
    val fromBalance: Double,
    val toBalance: Double,
)
