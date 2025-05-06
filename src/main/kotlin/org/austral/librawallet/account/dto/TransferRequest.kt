package org.austral.librawallet.account.dto

/**
 * Represents a peer-to-peer transfer request payload.
 */
data class TransferRequest(
    val toIdentifier: String,
    val identifierType: IdentifierType,
    val amount: Double,
)