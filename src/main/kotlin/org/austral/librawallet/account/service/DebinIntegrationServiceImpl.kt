package org.austral.librawallet.account.service

import org.austral.librawallet.account.dto.IdentifierType
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/**
 * Implementation of DebinIntegrationService that calls the external fake API.
 */
@Service
class DebinIntegrationServiceImpl(
    @Value("\${external.api.base-url}") private val baseUrl: String,
) : DebinIntegrationService {
    private val restTemplate = RestTemplate()

    override fun performDebin(
        identifierType: IdentifierType,
        fromIdentifier: String,
        amountInCents: Long,
    ): Boolean {
        println("Calling external service")
        val payload = mapOf(
            "identifier_type" to identifierType.name.lowercase(),
            "identifier" to fromIdentifier,
            "amount" to amountInCents,
        )
        return try {
            val response: ResponseEntity<Map<*, *>> = restTemplate.postForEntity(
                "$baseUrl/api/debin",
                HttpEntity(payload),
                Map::class.java,
            )
            println("External Server response: $response")
            response.statusCode == HttpStatus.OK
        } catch (_: Exception) {
            false
        }
    }
}
