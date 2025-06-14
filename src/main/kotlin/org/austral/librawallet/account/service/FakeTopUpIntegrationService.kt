package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.IdentifierType
import org.springframework.stereotype.Service

@Service
class FakeTopUpIntegrationService : TopUpIntegrationService {
    override fun performTopUp(
        identifierType: IdentifierType,
        toIdentifier: String,
        amountInCents: Long,
    ): Boolean {
        return when (identifierType) {
            IdentifierType.ALIAS -> {
                toIdentifier.firstOrNull()
                    ?.uppercaseChar()
                    ?.let { it in 'A'..'L' }
                    ?: false
            }

            IdentifierType.CVU -> {
                toIdentifier.firstOrNull()
                    ?.let { it in '0'..'4' }
                    ?: false
            }
        }
    }
}
