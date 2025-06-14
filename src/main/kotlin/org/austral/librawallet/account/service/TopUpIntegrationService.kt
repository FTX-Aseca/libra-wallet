package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.IdentifierType

/**
 * Interface for performing Top-up via an external API.
 */
interface TopUpIntegrationService {
    /**
     * Performs a top-up to the given identifier with the specified amount (in cents).
     * @param identifierType the type of identifier (ALIAS or CVU)
     * @param toIdentifier the alias or CVU of the target account
     * @param amountInCents the amount in cents to top up
     * @return true if the top-up succeeded, false otherwise
     */
    fun performTopUp(
        identifierType: IdentifierType,
        toIdentifier: String,
        amountInCents: Long,
    ): Boolean
} 
