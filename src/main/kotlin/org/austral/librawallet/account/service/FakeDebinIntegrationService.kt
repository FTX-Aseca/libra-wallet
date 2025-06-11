package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.IdentifierType
import org.springframework.stereotype.Service

/**
 * Fake implementation of DebinIntegrationService for testing and development.
 */
@Service
class FakeDebinIntegrationService : DebinIntegrationService {
    override fun performDebin(
        identifierType: IdentifierType,
        fromIdentifier: String,
        amountInCents: Long,
    ): Boolean {
        return when (identifierType) {
            IdentifierType.ALIAS -> {
                fromIdentifier.firstOrNull()
                    ?.uppercaseChar()
                    ?.let { it in 'A'..'L' }
                    ?: false
            }
            IdentifierType.CVU -> {
                fromIdentifier.firstOrNull()
                    ?.let { it in '0'..'4' }
                    ?: false
            }
        }
    }
}
