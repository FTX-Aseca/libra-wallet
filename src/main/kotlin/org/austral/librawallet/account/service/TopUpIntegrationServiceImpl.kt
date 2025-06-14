package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.IdentifierType
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * Implementation of TopUpIntegrationService that calls the external fake API.
 */
@Service
class TopUpIntegrationServiceImpl(
    @Value("\${external.api.base-url}") private val baseUrl: String,
) : TopUpIntegrationService {
    private val restTemplate = RestTemplate()

    override fun performTopUp(
        identifierType: IdentifierType,
        toIdentifier: String,
        amountInCents: Long,
    ): Boolean {
        val payload = mapOf(
            "identifier_type" to identifierType.name.lowercase(),
            "identifier" to toIdentifier,
            "amount" to amountInCents,
        )
        return try {
            val response: ResponseEntity<Map<*, *>> = restTemplate.postForEntity(
                "$baseUrl/api/topup",
                HttpEntity(payload),
                Map::class.java,
            )
            response.statusCode == HttpStatus.OK
        } catch (_: Exception) {
            false
        }
    }
}
