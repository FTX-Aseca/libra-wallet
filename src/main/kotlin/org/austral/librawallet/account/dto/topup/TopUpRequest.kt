package org.austral.librawallet.account.dto.topup

import org.austral.librawallet.account.dto.IdentifierType

/**
 * Request payload to simulate a top-up.
 */
data class TopUpRequest(
    val amount: Double,
    val identifierType: IdentifierType,
    val fromIdentifier: String,
)
