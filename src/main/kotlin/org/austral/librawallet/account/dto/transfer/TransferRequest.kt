package org.austral.librawallet.account.dto.transfer

import org.austral.librawallet.account.dto.IdentifierType

/**
 * Represents a peer-to-peer transfer request payload.
 */
data class TransferRequest(
    val toIdentifier: String,
    val identifierType: IdentifierType,
    val amount: Double,
)
