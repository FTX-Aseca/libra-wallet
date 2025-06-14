package org.austral.librawallet.account.dto.debin

/**
 * Response payload for DEBIN operations, containing the CVU and current balance.
 */
data class DebinResponse(
    val identifier: String,
    val amount: Double,
)
