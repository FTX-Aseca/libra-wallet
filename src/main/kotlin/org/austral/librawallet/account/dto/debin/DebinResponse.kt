package org.austral.librawallet.account.dto.debin

/**
 * Response payload for DEBIN operations.
 */
data class DebinResponse(
    val id: Long,
    val amount: Double,
    val status: String,
)
