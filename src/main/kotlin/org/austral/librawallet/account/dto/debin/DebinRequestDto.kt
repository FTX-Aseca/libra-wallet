package org.austral.librawallet.account.dto.debin

import org.austral.librawallet.account.dto.IdentifierType

/**
 * Request payload to simulate a DEBIN request directly.
 */
data class DebinRequestDto(
    val amount: Double,
    val identifierType: IdentifierType,
    val fromIdentifier: String,
)
