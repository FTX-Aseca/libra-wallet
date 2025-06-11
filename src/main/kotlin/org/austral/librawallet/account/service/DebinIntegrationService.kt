package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.IdentifierType

interface DebinIntegrationService {
    /**
     * Performs a DEBIN to the given identifier with the specified amount (in cents).
     * @param identifierType the type of identifier (ALIAS or CVU)
     * @param fromIdentifier the alias or CVU of the target account
     * @param amountInCents the amount in cents to debit
     * @return true if the DEBIN succeeded, false otherwise
     */
    fun performDebin(
        identifierType: IdentifierType,
        fromIdentifier: String,
        amountInCents: Long,
    ): Boolean
}
